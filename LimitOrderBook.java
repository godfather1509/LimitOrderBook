import java.util.HashMap;
import java.util.Map;

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
    Limit parent;
    Limit leftChild;
    Limit rightChild;
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
        Limit limit;
        Map<Integer, Limit> limitTree = order.buyOrSell ? buyLimits : sellLimits;

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
                if (order.buyOrSell)
                    buyLimits.remove(limit.limitPrice);
                else
                    sellLimits.remove(limit.limitPrice);
                updateBestPricesAfterRemoval(order.buyOrSell);
            }
        }
    }

    public void executeOrder(int orderId, int sharesToExecute) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.shares -= sharesToExecute;
            order.parentLimit.totalVolume -= sharesToExecute;

            if (order.shares <= 0) {
                cancelOrder(orderId); // Fully executed, remove order
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
        if (order.buyOrSell) {
            if (highestBuy == null || order.limit > highestBuy.limitPrice) {
                highestBuy = buyLimits.get(order.limit);
            }
        } else {
            if (lowestSell == null || order.limit < lowestSell.limitPrice) {
                lowestSell = sellLimits.get(order.limit);
            }
        }
    }

    private void updateBestPricesAfterRemoval(boolean isBuyOrder) {
        if (isBuyOrder) {
            highestBuy = buyLimits.values().stream().max((a, b) -> a.limitPrice - b.limitPrice).orElse(null);
        } else {
            lowestSell = sellLimits.values().stream().min((a, b) -> a.limitPrice - b.limitPrice).orElse(null);
        }
    }
}

public class LimitOrderBook {
    public static void main(String[] args) {
        Book book = new Book();

        // Add orders
        Order order1 = new Order(1, true, 100, 50); // Buy order
        Order order2 = new Order(2, false, 150, 55); // Sell order
        book.addOrder(order1);
        book.addOrder(order2);

        // Test best bid and offer
        System.out.println("Best Bid: " + book.getBestBid()); // Should print 50
        System.out.println("Best Offer: " + book.getBestOffer()); // Should print 55

        // Cancel an order and check updates
        book.cancelOrder(1);
        System.out.println("Best Bid after cancel: " + book.getBestBid()); // Should print -1 as no buy orders are left
    }
}
