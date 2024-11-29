import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class LimitOrderBook {
    public static void main(String[] args) {
        Book book = new Book();

        @SuppressWarnings("resource")
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.print("Please enter your Id Number: ");
            int idNumber = scan.nextInt();

            System.out.println("Select action:");
            System.out.println("1. Buy order");
            System.out.println("2. Sell order");
            int n = scan.nextInt();
            boolean action = n == 1;

            System.out.print("Enter quantity of shares: ");
            int amount = scan.nextInt();

            System.out.print("Enter limit: ");
            int limit = scan.nextInt();

            // Add orders
            Order order = new Order(idNumber, action, amount, limit);
            book.addOrder(order);

            // Display best bid and offer
            System.out.println("Best Bid: " + book.getBestBid());
            System.out.println("Best Offer: " + book.getBestOffer());

            // Prompt for order cancellation
            System.out.println("Do you want to cancel the order?");
            System.out.println("1. Yes \n2. No");
            int cancel = scan.nextInt();
            if (cancel == 1) {
                book.cancelOrder(idNumber);
                System.out.println("Best Bid after cancel: " + book.getBestBid());
                System.out.println("Best Offer after cancel: " + book.getBestOffer());
            }
        }
    }
}

class Order {
    int idNumber;
    boolean buyOrSell; // true for buy, false for sell
    int shares;
    int limit;
    long entryTime;
    long eventTime;
    Order nextOrder;
    Order prevOrder;
    Limit parentLimit;

    public Order(int idNumber, boolean buyOrSell, int shares, int limit) {
        this.idNumber = idNumber;
        this.buyOrSell = buyOrSell;
        this.shares = shares;
        this.limit = limit;
        this.entryTime = System.currentTimeMillis();
    }
}

class Limit {
    int limitPrice;
    int size;
    int totalVolume;
    Order headOrder;
    Order tailOrder;

    public Limit(int limitPrice) {
        this.limitPrice = limitPrice;
        this.size = 0;
        this.totalVolume = 0;
    }

    public void addOrder(Order order) {
        if (headOrder == null) {
            headOrder = order;
            tailOrder = order;
        } else {
            tailOrder.nextOrder = order;
            order.prevOrder = tailOrder;
            tailOrder = order;
        }
        order.parentLimit = this;
        size++;
        totalVolume += order.shares;
    }

    public void removeOrder(Order order) {
        if (order.prevOrder != null) {
            order.prevOrder.nextOrder = order.nextOrder;
        } else {
            headOrder = order.nextOrder;
        }
        if (order.nextOrder != null) {
            order.nextOrder.prevOrder = order.prevOrder;
        } else {
            tailOrder = order.prevOrder;
        }
        size--;
        totalVolume -= order.shares;
    }
}

class Book {
    Map<Integer, Order> orders = new HashMap<>();
    Map<Integer, Limit> buyLimits = new HashMap<>();
    Map<Integer, Limit> sellLimits = new HashMap<>();
    Limit highestBuy;
    Limit lowestSell;

    public void addOrder(Order order) {
        orders.put(order.idNumber, order);
        Map<Integer, Limit> limitTree = order.buyOrSell ? buyLimits : sellLimits;
        Limit limit;

        if (!limitTree.containsKey(order.limit)) {
            limit = new Limit(order.limit);
            limitTree.put(order.limit, limit);
            updateBestPrices(order);
        } else {
            limit = limitTree.get(order.limit);
        }
        limit.addOrder(order);
    }

    public void cancelOrder(int orderId) {
        Order order = orders.remove(orderId);
        if (order != null) {
            Limit limit = order.parentLimit;
            limit.removeOrder(order);

            if (limit.size == 0) {
                if (order.buyOrSell) {
                    buyLimits.remove(limit.limitPrice);
                } else {
                    sellLimits.remove(limit.limitPrice);
                }
                updateBestPricesAfterRemoval(order.buyOrSell);
            }
        }
    }

    public int getBestBid() {
        return highestBuy != null ? highestBuy.limitPrice : -1;
    }

    public int getBestOffer() {
        return lowestSell != null ? lowestSell.limitPrice : -1;
    }

    private void updateBestPrices(Order order) {
        if (order.buyOrSell) { // Buy order
            if (highestBuy == null || order.limit > highestBuy.limitPrice) {
                highestBuy = buyLimits.get(order.limit);
            }
        } else { // Sell order
            if (lowestSell == null || order.limit < lowestSell.limitPrice) {
                lowestSell = sellLimits.get(order.limit);
            }
        }
    }

    private void updateBestPricesAfterRemoval(boolean isBuyOrder) {
        if (isBuyOrder) {
            // Find the new highest buy limit
            highestBuy = buyLimits.values()
                                  .stream()
                                  .max((a, b) -> Integer.compare(a.limitPrice, b.limitPrice))
                                  .orElse(null);
        } else {
            // Find the new lowest sell limit
            lowestSell = sellLimits.values()
                                   .stream()
                                   .min((a, b) -> Integer.compare(a.limitPrice, b.limitPrice))
                                   .orElse(null);
        }
    }
}
