package clientPart1;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class StoreThread implements Runnable{

    private static final int OPERATING_HOURS = 9;
    private static final int SECOND_PHASE_START = 3;
    private static final int THIRD_PHASE_START = 5;

    private int storeID;
    private int customerPerStore;
    private int maxItemID;
    private int numOfPurchasePerHour;
    private int numOfItemPerPurchase;
    private String date;
    private String serverIP;

    private CountDownLatch threeHourGate;
    private CountDownLatch fiveHourGate;
    private CountDownLatch allStoresClosed;
    private AtomicBoolean reachThree;
    private AtomicBoolean reachFive;

    private AtomicInteger totalRequest;
    private AtomicInteger successRequest;

    public StoreThread(int storeID, int customerPerStore, int maxItemID, int numOfPurchasePerHour,
                       int numOfItemPerPurchase, String date, String serverIP,
                       CountDownLatch threeHourGate, CountDownLatch fiveHourGate, CountDownLatch allStoresClosed,
                       AtomicBoolean reachThree, AtomicBoolean reachFive, 
                       AtomicInteger totalRequest, AtomicInteger successRequest) {
        this.storeID = storeID;
        this.customerPerStore = customerPerStore;
        this.maxItemID = maxItemID;
        this.numOfPurchasePerHour = numOfPurchasePerHour;
        this.numOfItemPerPurchase = numOfItemPerPurchase;
        this.date = date;
        this.serverIP = serverIP;
        this.threeHourGate = threeHourGate;
        this.fiveHourGate = fiveHourGate;
        this.allStoresClosed = allStoresClosed;
        this.reachThree = reachThree;
        this.reachFive = reachFive;
        this.totalRequest = totalRequest;
        this.successRequest = successRequest;
    }

    @Override
    public void run() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(serverIP);
        PurchaseApi apiInstance = new PurchaseApi(apiClient);
        for(int h = 1; h < OPERATING_HOURS + 1; h++){
            for(int i = 0; i < this.getNumOfPurchasePerHour(); i++){
                makeOnePurchase(apiInstance);
            }
            if(!reachThree.get() && h == SECOND_PHASE_START){
                reachThree.set(true);
                threeHourGate.countDown();
            }else if(!reachFive.get() && h == THIRD_PHASE_START){
                reachFive.set(true);
                fiveHourGate.countDown();
            }
        }
        allStoresClosed.countDown();
    }

    public void makeOnePurchase(PurchaseApi apiInstance){

        Random random = new Random();

        // fill the body with default values
        Purchase body = new Purchase(); // Purchase | items purchased

        for(int i = 0; i < this.getNumOfItemPerPurchase(); i++){
            PurchaseItems item = new PurchaseItems();
            item.numberOfItems(1);
            item.itemID(Integer.toString(random.nextInt(100000) + 1));
            body.addItemsItem(item);
        }

        Integer custID = random.nextInt(this.getCustomerPerStore()) + getStoreID() * 1000;

        try {
            totalRequest.incrementAndGet();
            ApiResponse response = apiInstance.newPurchaseWithHttpInfo(body, this.getStoreID(), custID, this.getDate());
            if(response.getStatusCode() == 201){
                successRequest.incrementAndGet();
            }
//            System.out.println("store " + getStoreID() + ", returns: " + response.getStatusCode());
//            System.out.println("The customer is: " + custID);
        } catch (ApiException e) {
            System.err.println("Exception when calling PurchaseApi#newPurchase");
            e.printStackTrace();
        }
    }

    public int getStoreID() {
        return storeID;
    }

    public int getCustomerPerStore() {
        return customerPerStore;
    }

    public void setCustomerPerStore(int customerPerStore) {
        this.customerPerStore = customerPerStore;
    }

    public int getMaxItemID() {
        return maxItemID;
    }

    public void setMaxItemID(int maxItemID) {
        this.maxItemID = maxItemID;
    }

    public int getNumOfPurchasePerHour() {
        return numOfPurchasePerHour;
    }

    public void setNumOfPurchasePerHour(int numOfPurchasePerHour) {
        this.numOfPurchasePerHour = numOfPurchasePerHour;
    }

    public int getNumOfItemPerPurchase() {
        return numOfItemPerPurchase;
    }

    public void setNumOfItemPerPurchase(int numOfItemPerPurchase) {
        this.numOfItemPerPurchase = numOfItemPerPurchase;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }
}
