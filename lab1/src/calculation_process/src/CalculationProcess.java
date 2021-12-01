import os.lab1.compfuncs.basic.Disjunction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.NoSuchElementException;
import java.util.Optional;

class CalculationProcess {
    private SocketChannel sc;
    private final Optional<Boolean> result;
    private final long calcTime;

    CalculationProcess(char funcChar, int port) throws Exception {
        connectToServer(port);
        int x = readInput();
        long start, end;
        start = System.nanoTime();
        switch (funcChar) {
            case 'f':
            case 'F': {
                this.result = Disjunction.trialF(x);
                break;
            }
            case 'g':
            case 'G': {
                this.result = Disjunction.trialG(x);
                break;
            }
            default:
                throw new Exception("Function char must be either F or G");
        }
        end = System.nanoTime();
        calcTime = end - start;
        writeOutput(funcChar);
    }

    private void connectToServer(int port) throws IOException {
        sc = SocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", port);
        sc.connect(hostAddress);
    }

    private int readInput() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        sc.read(buffer);
        buffer.rewind();
        return buffer.getInt();
    }

    public void writeOutput(char funcChar) throws IOException, NoSuchElementException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + Long.BYTES + Character.BYTES);
        buffer.putInt(result.orElse(false) ? 1 : 0);
        buffer.putLong(calcTime);
        buffer.putChar(funcChar);
        buffer.rewind();
        sc.write(buffer);
        sc.close();
    }
}
