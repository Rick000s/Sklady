package com.example.sklady.datastructures;

import java.util.Iterator;

public class AbstractDoubleList<T> implements IAbstrDoubleList<T> {

        private static class Node<T> {          //статичний бо незалежна ячейка повинна бути і це швидше
                T data;
                Node<T> next;
                Node<T> prev;

                public Node(T data) {           //конструктор
                        this.prev = null;
                        this.data = data;
                        this.next = null;
                }
        }

        private Node<T> first;
        private Node<T> current;
        private Node<T> last;

        public AbstractDoubleList() {
                this.first = null;
                this.current = null;
                this.last = null;
        }

        @Override
        public boolean jePrazdny() {
                return first == null;
        }

        @Override
        public void vlozPrvni(T data) {
        Node<T> newNode = new Node<>(data);
                if(jePrazdny()) {
                        first = newNode;
                        last = newNode;
                } else {
                        newNode.next = first;   //новий стає на місце 1 та міняє "наступний" на старий
                        first.prev = newNode;   //старий стає на місце 2 та міняє "минулий" на новий
                        first = newNode;
                }
                current = newNode;
        }

        @Override
        public void vlozPosledni(T data) {
        Node<T> newNode = new Node<>(data);
                if(jePrazdny()) {
                        first = newNode;
                        last = newNode;
                } else {
                        newNode.prev = last;    //новому "наступний" міняє на те що було у минулого
                        last.next = newNode;    //минулому останньому  ми декларуємо нову позицію
                        last = newNode;         //тепер останнім стає цей
                }
                current = newNode;
        }

        @Override
        public void vlozPredchudce(T data) {
                if (jePrazdny()) {
                        vlozPrvni(data);
                        return;
                }

                Node<T> newNode = new Node<>(data);

                newNode.next = current; //присвоєння новому вперед
                newNode.prev = current.prev; //присвоєння новому назад

                if (current.prev != null) {
                        current.prev.next = newNode; //зміна попередньому звязку вперед
                } else {
                        first = newNode;
                }

                current.prev = newNode; // підвязка наступному нового назад
                current = newNode;
        }

        @Override
        public void vlozNaslednika(T data) {
                if (jePrazdny()) {
                        vlozPrvni(data);
                        return;
                }

                Node<T> newNode = new Node<>(data);

                newNode.prev = current; //присвоєння новому назад
                newNode.next = current.next; //присвоєння новому вперед

                if (current.next != null) {
                        current.next.prev = newNode; //зміна наступному звязку назад
                } else {
                        last = newNode;
                }

                current.next = newNode; // підвязка минулому нового наступному
                current = newNode;
        }

        @Override
        public T zpristupniAktualni() {
                if (jePrazdny() || current == null) {
                        return null;
                }
                return current.data;
        }

        @Override
        public T zpristupniPrvni() {
                if (jePrazdny()) {
                        return null;
                }
                current = first;
                return current.data;
        }

        @Override
        public T zpristupniPosledni() {
                if (jePrazdny()) {
                        return null;
                }
                current = last;
                return current.data;
        }

        @Override
        public T zpristupniNaslednika() {
                if (jePrazdny() || current == null || current.next == null) { // нема куди йти вперед
                        return null;
                }
                current = current.next; // переходимо на наступний
                return current.data;
        }

        @Override
        public T zpristupniPredchudce() {
                if (jePrazdny() || current == null || current.prev == null) { // нема куди йти назад
                        return null;
                }
                current = current.prev; // переходимо на попередній
                return current.data;
        }

        @Override
        public T odeberAktualni() {
                if (jePrazdny() || current == null) {
                        return null;
                }

                T data = current.data; // зберігаємо дані щоб повернути їх

                if (current == first && current == last) { // якщо це єдиний елемент у списку
                        first = null;
                        last = null;
                }

                else if (current == first) { // якщо видаляємо перший елемент
                        first = first.next;
                        first.prev = null;
                }

                else if (current == last) { // якщо видаляємо останній елемент
                        last = last.prev;
                        last.next = null;
                }

                else {                      // якщо видаляємо десь у середині
                        current.prev.next = current.next;
                        current.next.prev = current.prev;
                }
                current = first;
                return data;
        }

        @Override
        public T odeberPrvni() {
                if (jePrazdny()) {
                        return null;
                }
                current = first;
                return odeberAktualni();
        }

        @Override
        public T odeberPosledni() {
                if (jePrazdny()) {
                        return null;
                }
                current = last;
                return odeberAktualni();
        }

        @Override
        public T odeberNaslednika() {
                if (jePrazdny() || current == null || current.next == null) {
                        return null;
                }
                current = current.next;
                return odeberAktualni();
        }

        @Override
        public T odeberPredchudce() {
                if (jePrazdny() || current == null || current.prev == null) {
                        return null;
                }
                current = current.prev;
                return odeberAktualni();
        }

        @Override
        public void zrus() {
                first = null;
                last = null;
                current = null;
        }

        @Override
        public Iterator<T> iterator() {
                return new Iterator<T>() {
                        private Node<T> currentNode = first; // починаємо з першого елемента

                        @Override
                        public boolean hasNext() {
                                return currentNode != null; // чи є ще елементи попереду?
                        }

                        @Override
                        public T next() {
                                if (!hasNext()) {
                                        throw new java.util.NoSuchElementException();
                                }
                                T data = currentNode.data; // зберігаємо дані поточного
                                currentNode = currentNode.next; // зсуваємося на наступний вузол
                                return data;
                        }
                };
        }
}
