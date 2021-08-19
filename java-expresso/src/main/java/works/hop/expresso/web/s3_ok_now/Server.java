package works.hop.expresso.web.s3_ok_now;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<SocketChannel, ByteBuffer> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        InetSocketAddress hostAddr = new InetSocketAddress("localhost", 8888);
        serverChannel.bind(hostAddr);
        serverChannel.configureBlocking(false);

        //register a selector
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            System.out.println("Awaiting connection request...");
            selector.select();

            //handle event
            Set<SelectionKey> keys = selector.selectedKeys();
            System.out.printf("Selection keys - size: %d%n", keys.size());
            Iterator<SelectionKey> iter = keys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (!key.isValid()) {
                    continue;
                }

                try {
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    }
                } catch (IOException e) {
                    System.err.println(e);
                }

                //clean up closed connections
                activeClients.keySet().removeIf(client -> !client.isOpen());
            }
        }
    }

    private static void write(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel(); //channel here should be the client's SocketChannel
        ByteBuffer buffer = activeClients.get(clientChannel);
        //attempt to write a much as possible
        clientChannel.write(buffer);
        if (!buffer.hasRemaining()) {
            buffer.compact(); //make buffer ready for read operation
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private static void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel(); //channel here should be the client's SocketChannel
        ByteBuffer buffer = activeClients.get(clientChannel);
        int data = clientChannel.read(buffer);
        if (data == -1) {
            clientChannel.close();
            activeClients.remove(clientChannel);
        } else {
            //socket must still be active
            buffer.flip();  //prepare buffer for write operation
            handle(buffer);
            key.interestOps(SelectionKey.OP_WRITE); //signal selector that it can write response
        }
    }

    private static void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel(); //channel here should be server's ServerSocketChannel
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(), SelectionKey.OP_READ);
        activeClients.put(clientChannel, ByteBuffer.allocate(256));
    }

    private static void handle(ByteBuffer buffer) {
        for(int i = 0; i < buffer.limit(); i++){
            int ch = buffer.get(i);
            buffer.put(i, (byte) (Character.isLetter(ch) ? (ch ^ ' ') : ch));
        }
    }
}
