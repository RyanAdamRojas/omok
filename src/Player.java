// Authors: Ryan Adam Rojas, Sophia Montenegro

import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

public abstract class Player {
    private String name;
    private String symbol;

    Player(String name, String symbol){
        this.name = name;
        this.symbol = symbol;
    }

    public abstract String requestMove(Board board, Scanner scanner, PrintStream printStream) throws IOException;//Every abstract class must have at least one abstract method

    //Getters & Setter
    public String getName() {
        return name;
    }
    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public boolean equals(Object other) {
        //Intellij automatically creates an override for equals
        if (this == other) return true;
        if (!(other instanceof Player player)) return false;
        return Objects.equals(getName(), player.getName()) && Objects.equals(getSymbol(), player.getSymbol());
    }

    @Override
    public int hashCode() {
        //Intellij automatically creates an override for hashCode
        return Objects.hash(getName(), getSymbol());
    }
}
