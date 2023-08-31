package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static List<Book> books = new ArrayList<>();
    private static AtomicInteger idGenerator = new AtomicInteger(1);
    private static ObjectMapper objectMapper = new ObjectMapper(); // Создаем объект ObjectMapper

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/books", new UsersHandler());
        server.start();
        System.out.println("Server started on port 8080");

    }



    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String responseBody = objectMapper.writeValueAsString(books);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBody.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody.getBytes());
                }
            } else if ("POST".equals(exchange.getRequestMethod())) {
                int newId = idGenerator.getAndIncrement();
                Scanner sc = new Scanner(System.in);
                System.out.printf("Input books title: ");
                String name = sc.nextLine();
                System.out.printf("Input author's name: ");
                String author = sc.nextLine();
                System.out.printf("Input release year: ");
                int year = sc.nextInt();
                System.out.println("OK!");
                Book newBook = new Book(newId, name, author, year);
                books.add(newBook);
                String responseBody = objectMapper.writeValueAsString(newBook);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(201, responseBody.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody.getBytes());
                }
            } else if ("DELETE".equals(exchange.getRequestMethod())) {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                if (pathParts.length == 3) {
                    int bookId = Integer.parseInt(pathParts[2]);
                    books.removeIf(book -> book.getId() == bookId);
                    String responseBody = "{\"message\":\"Book with ID " + bookId + " removed.\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, responseBody.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBody.getBytes());
                    }
                }
            } else {
                String responseBody = "{\"error\":\"Method not allowed\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(405, responseBody.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody.getBytes());
                }
            }
        }
    }

    static class Book {
        private int id;
        private String title;

        private String author;

        private int releaseYear;

        public Book(int id, String title, String author, int releaseYear) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.releaseYear = releaseYear;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Book {" +
                    "id=" + id +
                    ", title ='" + title +
                    ", title ='" + title +
                    "year=" + releaseYear +
                    '\'' +
                    '}';
        }
    }
}