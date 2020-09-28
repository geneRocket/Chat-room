
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.channels.*;
import java.net.InetSocketAddress;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class SocketServer {
    private int PORT=1115;
    private Selector selector;
    private ServerSocketChannel listen_serverSocketChannel;
    private ThreadPoolExecutor threadPoolExecutor= new ThreadPoolExecutor(1,2,5, TimeUnit.SECONDS,new LinkedBlockingQueue<>(),new ThreadPoolExecutor.CallerRunsPolicy());

    UserManager userManager=new UserManager();
    final private String server_name="Server";

    public SocketServer(){
    }

    void listen_accept() throws IOException {
        selector=Selector.open();
        listen_serverSocketChannel =ServerSocketChannel.open();
        listen_serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        listen_serverSocketChannel.configureBlocking(false);
        listen_serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    void loginMessageHandle(Message message,SocketChannel socketChannel,SelectionKey key) throws IOException {
        String user=message.messageHeader.sender;
        Message response = new Message();
        response.messageHeader.sender=server_name;
        if(userManager.add_user(user,key)){
            response.messageHeader.Type=MessageType.SUCCESS;
        }
        else{
            response.messageHeader.Type=MessageType.FAIL;
        }
        NetUtil.send_message(response,socketChannel);
    }

    void logoutMessageHandle(Message message)  {
        String user=message.messageHeader.sender;
        userManager.remove_user(user);
    }

    void broadcastMessage(Message message) throws IOException {
        Set<SelectionKey> keyset=selector.keys();

        for (SelectionKey selectionKey: keyset) {
            Channel channel = selectionKey.channel();
            if(channel instanceof SocketChannel){

                SocketChannel socketChannel=(SocketChannel)channel;
                if(socketChannel.isConnected()){
                    NetUtil.send_message(message,socketChannel);
                }
            }
        }
    }

    void sendMessageHandle(Message message) throws IOException {
        String user=message.messageHeader.sender;
        String content=message.body;
        long timestamp=message.messageHeader.timestamp;

//        System.out.println(user+":"+TimeUtil.get_date_string(timestamp));
//        System.out.println(content);
//        System.out.println();

        broadcastMessage(message);
    }

    void read_handle(SelectionKey key) throws IOException {
        SocketChannel socketChannel=(SocketChannel) key.channel();

        Message message= NetUtil.receive_message(socketChannel);
        if(message==null){
            userManager.remove_user(key);
            NetUtil.close_socketchannel(key);
            return;
        }


        System.out.println(message);

        if(message.messageHeader.Type==MessageType.LOGIN){
            loginMessageHandle(message,socketChannel,key);
        }
        else if(message.messageHeader.Type==MessageType.SEND){
            sendMessageHandle(message);
        }
        else if(message.messageHeader.Type==MessageType.LOGOUT){
            logoutMessageHandle(message);
            NetUtil.close_socketchannel(key);
        }

        key.interestOps(key.interestOps() | ( SelectionKey.OP_READ));
        key.selector().wakeup();
    }

    void handle_accept() throws IOException {
        SocketChannel client= listen_serverSocketChannel.accept();
        client.configureBlocking(false);
        client.register(selector,SelectionKey.OP_READ);
    }

    void select() throws IOException {
        while (true){
            selector.select();
            Iterator<SelectionKey> iter=selector.selectedKeys().iterator();
            while (iter.hasNext()){
                SelectionKey key=iter.next();
                iter.remove();

                if(key.isAcceptable()){
                    handle_accept();
                }
                else if(key.isReadable()){
                    key.interestOps(key.interestOps() & (~ SelectionKey.OP_READ));
                    Runnable runnable =new Runnable() {
                        @SneakyThrows
                        @Override
                        public void run() {
                            read_handle(key);
                        }
                    };
                    threadPoolExecutor.submit(runnable);
                }
            }
        }

    }

    public static void main(String[] args) {
        try {
            SocketServer socketServer= new SocketServer();
            socketServer.listen_accept();
            socketServer.select();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}