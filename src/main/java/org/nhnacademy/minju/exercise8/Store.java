package org.nhnacademy.minju.exercise8;

import java.util.concurrent.Semaphore;

/**
 * 매장은 물건을 납품 받아서 판매한다.
 * 매장에는 최대 10개의 물건만 전시할 수 있다.
 * 매장은 최대 5명까지만 동시 입장 가능하다. -> thread 개수
 * 매장에서 물건 구매는 동시에 1명만 가능하다. -> synchronized
 * 매장에서 물건 판매 후 빈 공간에 생기면 생산자에게 알려 준다.
 * 매장에서 물건 납품은 동시에 1명만 가능하다.
 * 매장에서 물건이 들어오면 소비자에게 알려 준다.
 */
public class Store {

    private Semaphore entry; // 리소스에 접근할 수 있는 스레드의 수를 제한
    private int itemCount;

    public Store() {
        entry = new Semaphore(5);
        itemCount = 10;
    }

    public void enter() {
        try {
            entry.acquire(); // 메소드를 통하여 접근 권한을 허가 받고, 남은 퍼밋이 없을 경우 퍼밋이 생기거나 인터럽트가 발생되거나 타임아웃이 걸리지 전까지 대기
            System.out.println(Thread.currentThread().getName() + " 입장");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void exit() {
        System.out.println(Thread.currentThread().getName() + " 퇴장");
        entry.release(); // 확보했던 퍼밋을 다시 세마포어에게 돌려준다
        Thread.currentThread().interrupt();
    }

    public synchronized void buy() {
        while (itemCount <= 0) {
            try {
                System.out.println(Thread.currentThread().getName() + " 구매 대기");
                wait();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        itemCount--;
        System.out.println("구매 완료, 재고 : " + itemCount);
        notify();
    }

    public synchronized void sell() {
        while (itemCount >= 10) {
            try {
                System.out.println("재고를 채울 필요 없다. 재고 : " + itemCount);
                wait();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        itemCount++;
        System.out.println("판매 후 재고 채우기 완료, 재고 : " + itemCount);
        notify();
    }

    public static void main(String[] args) {

        Store store = new Store();
        Seller seller = new Seller(store);
        seller.start();

        for (int i = 0; ; i++) {
            Buyer buyer = new Buyer("Buyer " + i, store);
            buyer.start();
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                System.out.println("terminated");
                Thread.currentThread().interrupt();
            }
        }
    }
}