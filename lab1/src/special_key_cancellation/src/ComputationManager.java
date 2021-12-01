import sun.misc.Signal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class ComputationManager {
    private final AsynchronousServerSocketChannel server;
    private static final int PORT = 7272;

    private final int functsAmount;
    private final List<Integer> args;
    private final List<Process> processesF;
    private final List<Process> processesG;
    private boolean cancelled = false;
    private Boolean result = null;

    public ComputationManager(int functionsAmount, List<Integer> args) throws IOException {
        server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress("localhost", PORT));
        this.functsAmount = functionsAmount;
        this.args = args;
        this.processesF = new ArrayList<>();
        this.processesG = new ArrayList<>();
    }

    private void runProcess(String fileWithProcess, List<Process> processes) throws IOException {
        Process process = Runtime.getRuntime().exec(
                "java -cp " + "out\\artifacts\\calculation_process\\calculation_processes.jar "
                        + fileWithProcess + " " + PORT);
        processes.add(process);
    }

    private List<AsynchronousSocketChannel> connectClients(int clientsAmount) throws ExecutionException, InterruptedException {
        List<AsynchronousSocketChannel> clients = new ArrayList<>(clientsAmount);
        for (int i = 0; i < clientsAmount; i++) {
            Future<AsynchronousSocketChannel> client = server.accept();
            clients.add(client.get());
        }
        return clients;
    }

    private void passArguments(List<AsynchronousSocketChannel> clients) throws ExecutionException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        for (int i = 0; i < clients.size(); i++) {
            buffer.putInt(args.get(i));
            buffer.rewind();
            Future<Integer> writeFuture = clients.get(i).write(buffer);
            writeFuture.get();
            buffer.clear();
        }
    }

    private List<AsynchronousSocketChannel> prepareCalcClients(char funcType, String fileWithProcess)
            throws IOException, ExecutionException, InterruptedException {
        List<Process> processes;
        switch (funcType) {
            case 'f', 'F' -> processes = processesF;
            case 'g', 'G' -> processes = processesG;
            default -> throw new IllegalArgumentException("Function type must be either F or G");
        }

        for (int i = 0; i < functsAmount / 2; i++) {
            runProcess(fileWithProcess, processes);
        }
        List<AsynchronousSocketChannel> clients = connectClients(functsAmount / 2);
        passArguments(clients);
        return clients;
    }

    private List<Future<Integer>> initResultFutures(List<AsynchronousSocketChannel> clients,
                                                    ByteBuffer buffer) {
        List<Future<Integer>> futures = new ArrayList<>(clients.size());
        for (AsynchronousSocketChannel client : clients) {
            futures.add(client.read(buffer));
        }
        return futures;
    }

    private static class Result {
        boolean result;
        char funcChar;
        long calcTime;

        Result(ByteBuffer buffer) {
            buffer.rewind();
            this.result = buffer.getInt() == 1;
            this.calcTime = buffer.getLong();
            this.funcChar = buffer.getChar();
        }
    }

    private Result getResult(Future<Integer> resultFuture, ByteBuffer buffer)
            throws ExecutionException, InterruptedException {
        resultFuture.get();
        buffer.rewind();
        return new Result(buffer);
    }

    private int checkFuturesForResult(List<Future<Integer>> futures, char funcType, ByteBuffer buffer)
            throws ExecutionException, InterruptedException {
        List<Process> processes;
        switch (funcType) {
            case 'f', 'F' -> processes = processesF;
            case 'g', 'G' -> processes = processesG;
            default -> throw new IllegalArgumentException("Function type must be either F or G");
        }

        for (Future<Integer> resultFuture : futures) {
            if (resultFuture.isDone()) {
                Result currResult = getResult(resultFuture, buffer);
                buffer.clear();

                // XOR
                if (result == null) {
                    result = currResult.result;
                } else {
                    result = !(result == currResult.result);
                }

                System.out.printf("[ It took %d micros to compute '%b' in %c function ]\n", currResult.calcTime / 1000, currResult.result, funcType);

                futures.remove(resultFuture);
                if (futures.size() == 0) {
                    for (Process process : processes) {
                        process.destroy();
                    }
                    return 0;
                }
                return 1;
            }
        }
        return -1;
    }

    private void printResult(List<Future<Integer>> resultFuturesF,
                             List<Future<Integer>> resultFuturesG,
                             ByteBuffer resultBuffer)
            throws ExecutionException, InterruptedException {
        // try retrieving the result after cancellation
        if (cancelled) {
            System.out.println("Computation was cancelled.");

            if (resultFuturesF.size() != 0 || resultFuturesG.size() != 0) {
                int checkResultF = checkFuturesForResult(resultFuturesF, 'f', resultBuffer);
                int checkResultG = checkFuturesForResult(resultFuturesG, 'g', resultBuffer);
                if (checkResultF != 0 || checkResultG != 0) {
                    System.out.println(resultFuturesF.size() + " F functions were not calculated.");
                    System.out.println(resultFuturesG.size() + " G functions were not calculated.");
                    return;
                }
            } else {
                System.out.println("But result was retrieved...");
            }
        }

        System.out.println("Result of computation: " + result + ".");
    }

    public void run() throws IOException, ExecutionException, InterruptedException {
        Signal.handle(new Signal("INT"), signal -> cancelled = true);

        List<AsynchronousSocketChannel> FClients = prepareCalcClients('f', "ProcessF");
        List<AsynchronousSocketChannel> GClients = prepareCalcClients('g', "ProcessG");

        ByteBuffer resultBuffer = ByteBuffer.allocate(Integer.BYTES + Long.BYTES + Character.BYTES);
        List<Future<Integer>> resultFuturesF = initResultFutures(FClients, resultBuffer);
        List<Future<Integer>> resultFuturesG = initResultFutures(GClients, resultBuffer);

        // * is a binary operation

        // firstly we compute f1 * f2 * ... * fn
        while (!resultFuturesF.isEmpty()) {
            if (cancelled) {
                break;
            }

            int checkResult = checkFuturesForResult(resultFuturesF, 'f', resultBuffer);
            if (checkResult == 0) {
                break;
            }
        }

        // then we compute result * g1 * g2 * ... * gn
        while (!resultFuturesG.isEmpty()) {
            if (cancelled) {
                break;
            }

            int checkResult = checkFuturesForResult(resultFuturesG, 'g', resultBuffer);
            if (checkResult == 0) {
                break;
            }
        }

        printResult(resultFuturesF, resultFuturesG, resultBuffer);
    }
}
