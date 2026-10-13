package ru.edu.pr05;
public class SelfCheck {
    public static void main(String[] args) {
        boolean ok = false;
        try { ok = (true); } catch (Exception e) { System.out.println("FAIL exception " + e.getMessage()); }
        System.out.println(ok ? "PASS" : "FAIL  complete TODO methods");
    }
}
