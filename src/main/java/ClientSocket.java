import lombok.SneakyThrows;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientSocket {
    String host = "127.0.0.1";
    int port = 1115;

    Socket socket;
    ReadHandle readHandle;

    String username;

    public ClientSocket(String user_) throws IOException {
        socket = new Socket(host,port);
        readHandle=new ReadHandle();
        username =user_;
    }

    boolean login() throws IOException {
        Message request = new Message();
        request.messageHeader.sender= username;
        request.messageHeader.Type=MessageType.LOGIN;
        NetUtil.send_message(request,socket);

        Message response = NetUtil.receive_message(socket);
        return response != null && response.messageHeader.Type == MessageType.SUCCESS;
    }

    void logout() throws IOException {
        Message request = new Message();
        request.messageHeader.sender= username;
        request.messageHeader.Type=MessageType.LOGOUT;
        NetUtil.send_message(request,socket);
    }

    void sendMessage(String content) throws IOException {
        Message request = new Message();
        request.messageHeader.sender= username;
        request.messageHeader.Type=MessageType.SEND;
        request.body=content;
        NetUtil.send_message(request,socket);
    }

    void receiveMessageHandle(Message message){
        String user=message.messageHeader.sender;
        String content=message.body;
        long timestamp=message.messageHeader.timestamp;
        System.out.println(user+":"+TimeUtil.get_date_string(timestamp));
        System.out.println(content);
        System.out.println();
    }

    void shutdown() throws InterruptedException, IOException {
        logout();
        readHandle.close_receive();
        readHandle.join();
        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }

    void launch() throws IOException, InterruptedException {
        if(login()){
            System.out.println("login success");
            readHandle.start();

            for (int i=0;i<100;i++){
                sendMessage(Integer.valueOf(i).toString());
            }

            Scanner scanner=new Scanner(System.in);
            String input;
            while (scanner.hasNext()){
                input=scanner.nextLine();
                if(input.equals("quit")){
                    break;
                }
                sendMessage(input);
            }

            shutdown();
        }
        else {
            System.out.println("login fail");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ClientSocket clientSocket=new ClientSocket("wwj");
        clientSocket.launch();
    }

    class ReadHandle extends Thread{
        volatile boolean isclose=false;

        @SneakyThrows
        @Override
        public void run() {
            while(!isclose){
                Message message=NetUtil.receive_message(socket);
                if(message== null){
                    return;
                }
                if(message.messageHeader.Type==MessageType.SEND){
                    receiveMessageHandle(message);
                }
            }
            System.out.println("readhandle close");
        }

        void close_receive(){
            isclose=true;
        }
    }



}