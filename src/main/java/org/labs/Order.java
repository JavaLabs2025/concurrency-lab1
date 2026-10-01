package org.labs;

import java.util.concurrent.CompletableFuture;

/**
 * Заказ добавки: кто просит и «квитанция», по которой он получит ответ.
 * Официант выполняет её: true — суп принесён, false — еда кончилась.
 */
record Order(int programmerId, CompletableFuture<Boolean> plate) {
}