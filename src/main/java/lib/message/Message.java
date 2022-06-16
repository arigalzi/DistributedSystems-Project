package lib.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public interface Message {
    char getType();

    InetAddress getSource();

    static Message parseString(String text, InetAddress sender) throws UnknownHostException {
        Scanner scanner = new Scanner(text);
        System.out.println("PARSING: " + text);
        scanner.useDelimiter("\\|");
        String type = scanner.next();
        int sequenceNumber;
        InetAddress target;
        switch (type) {
            case "T": // content message (normal)
                sequenceNumber = scanner.nextInt();
                String content = scanner.next();
                return new TextMessage(sender, content, sequenceNumber);
            case "A": // ack
                target = InetAddress.getByName(scanner.next());
                sequenceNumber = scanner.nextInt();
                return new AckMessage(sender, target, sequenceNumber);
            case "N": // nack
                String stringTarget = scanner.next();
                target = InetAddress.getByName(stringTarget);
                sequenceNumber = scanner.nextInt();
                return new NackMessage(sender, target, sequenceNumber);
            case "J": // join
                sequenceNumber = scanner.nextInt();
                return new JoinMessage(sender, sequenceNumber);
            case "L": // leave
                sequenceNumber = scanner.nextInt();
                return new LeaveMessage(sender, sequenceNumber);
            case "P": // ping
                return new PingMessage(sender);
            case "V": // viewchange
                int elements = scanner.nextInt();
                List<InetAddress> newView = new ArrayList<>();
                for (int i = 0; i < elements; ++i)
                    newView.add(InetAddress.getByName(scanner.next()));
                return new ViewChangeMessage(sender, newView);
            case "F":
                return new FlushMessage(sender);
            default:
                return new ErrorMessage(sender);
        }
    }

    String getTransmissionString();
}
