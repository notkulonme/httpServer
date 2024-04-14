package Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Logger {
    String PATH;

    public Logger(String PATH) {
        this.PATH = PATH;
    }

    public void log(Object logInf) {
        String inf = "\tLogger: " + LocalDate.now() + " " + LocalTime.now() + " " + logInf;
        System.out.println(inf);
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(PATH, true));
            writer.write(inf + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("\tFailure while writing to the file");
        }

    }

}
