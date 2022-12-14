package lib.supervisor.state;

import lib.Settings;
import lib.message.JoinMessage;
import lib.message.Message;
import lib.message.ViewChangeMessage;
import lib.supervisor.Supervisor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NormalState extends SupervisorState {
    private Boolean running;
    private Thread pingingThread;

    /**
     * Constructor
     * @param supervisor is the process that will be the supervisor
     * This function includes the actions of the supervisor which after adding the
     * process to the {@Link viewTimer} and the starts the thread that will manage the
     * ping messages and processes disconnections.
     */
    public NormalState(Supervisor supervisor) {
        super(supervisor);
        System.out.println("[SUPERVISOR] normal state with view: " + supervisor.getView());
        supervisor.getViewTimers().clear();
        for (InetAddress addr : supervisor.getView()) {
            supervisor.getViewTimers().put(addr, 0);
        }
        running = true;

        pingingThread = new Thread(() -> {
            while (running) {
                try {
                    Map<InetAddress, Integer> viewTimers = supervisor.getViewTimers();
                    List<InetAddress> disconnected = new ArrayList<>();
                    for (InetAddress addr : viewTimers.keySet()) {
                        int time = viewTimers.get(addr);
                        time += Settings.PING_PERIOD;
                        viewTimers.put(addr, time);
                        if (time >= Settings.PING_TIMEOUT) {
                            disconnected.add(addr);
                        }
                    }
                    if (running) supervisor.publishDisconnected(disconnected);
                    Thread.sleep(Settings.PING_PERIOD);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        pingingThread.start();
    }

    private void stopPingingThread() {
        running = false;
    }

    /**
     * This function has to start the view change process through {@Link viewChangeMessage}
     * when a message of type join is received.
     * @param m message to be processed
     */
    @Override
    public SupervisorState processMessage(Message m) throws IOException {
        if (m.getType() == 'J') {
            JoinMessage joinMessage = (JoinMessage) m;
            supervisor.getView().add(joinMessage.getSource());

            stopPingingThread();
            ViewChangeMessage viewChangeMessage = new ViewChangeMessage(supervisor.getMyAddress(), supervisor.getView());
            supervisor.sendMessage(viewChangeMessage);
            return new ViewInstallationState(supervisor);
        }

        return this;
    }
    /**
     * This function removes all processes that are disconnected from the view
     * and starts the view change process.
     * @param disconnectList is the list of process that has to be disconnected
     */
    @Override
    public SupervisorState processDisconnect(List<InetAddress> disconnectList) throws IOException {
        System.out.println("[SUPERVISOR] disconnecting: " + disconnectList);
        supervisor.getView().removeAll(disconnectList);

        stopPingingThread();
        ViewChangeMessage viewChangeMessage = new ViewChangeMessage(supervisor.getMyAddress(), supervisor.getView());
        supervisor.sendMessage(viewChangeMessage);
        if (supervisor.getView().isEmpty()) {
            return new NormalState(supervisor);
        } else {
            return new ViewInstallationState(supervisor);
        }
    }
}