import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NetUtil {

    static private final int int_size=4;

    public static byte[] read_given_len_bytes(InputStream inputStream, int given_len) throws IOException {
        byte[] bytes=new byte[given_len];
        int offset=0;
        while (given_len>0){
            int read_len=inputStream.read(bytes,offset,given_len);
            if(read_len==-1){
                return null;
            }
            offset+=read_len;
            given_len-=read_len;
        }
        return bytes;
    }

    public static ByteBuffer read_given_len_bytes(SocketChannel socketChannel, int given_len) throws IOException {
        ByteBuffer bytebuffer=ByteBuffer.allocate(given_len);
        while (bytebuffer.hasRemaining()){
            int len=socketChannel.read(bytebuffer);
            if(len==-1)
                return null;
        }
        bytebuffer.flip();
        return bytebuffer;
    }

    public static int bytes2int(byte[] bytes){
        int result = 0;
        if(bytes.length == int_size){
            int a = (bytes[0] & 0xff) << 24;
            int b = (bytes[1] & 0xff) << 16;
            int c = (bytes[2] & 0xff) << 8;
            int d = (bytes[3] & 0xff);
            result = a | b | c | d;
        }
        return result;
    }

    public static byte[] read_a_packet(InputStream inputStream) throws IOException {
        byte[] int_bytes=read_given_len_bytes(inputStream,int_size);

        if(int_bytes==null)
            return null;

        int len= NetUtil.bytes2int(int_bytes);
        return NetUtil.read_given_len_bytes(inputStream,len);
    }

    public static ByteBuffer read_a_packet(SocketChannel socketChannel) throws IOException {
        ByteBuffer int_buffer=read_given_len_bytes(socketChannel,int_size);

        if(int_buffer==null)
            return null;

        int len= NetUtil.bytes2int(int_buffer.array());
        return NetUtil.read_given_len_bytes(socketChannel,len);
    }

    public static ByteBuffer pack_a_packet(byte[] bytes){
        ByteBuffer byteBuffer=ByteBuffer.allocate(int_size+bytes.length);
        byteBuffer.putInt(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return byteBuffer;
    }

    static void send_message(Message message, Socket socket) throws IOException {
        message.messageHeader.timestamp=System.currentTimeMillis();
        String json = JSON.toJSONString(message);
        byte[] json_bytes=json.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bytebuffer= NetUtil.pack_a_packet(json_bytes);
        socket.getOutputStream().write(bytebuffer.array());

    }

    static void send_message(Message message, SocketChannel socketChannel) throws IOException {
        message.messageHeader.timestamp=System.currentTimeMillis();
        String json = JSON.toJSONString(message);
        byte[] json_bytes=json.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bytebuffer= NetUtil.pack_a_packet(json_bytes);
        socketChannel.write(bytebuffer);
    }

    public static Message receive_message(Socket socket) throws IOException {
        InputStream inputStream=socket.getInputStream();
        byte[] string_bytes= NetUtil.read_a_packet(inputStream);
        if(string_bytes==null)
            return null;
        return JSON.parseObject(new String(string_bytes, StandardCharsets.UTF_8), Message.class);

    }

    public static Message receive_message(SocketChannel socketChannel) throws IOException {
        ByteBuffer string_byteBuffer= NetUtil.read_a_packet(socketChannel);
        if(string_byteBuffer==null){
            return null;
        }
        return JSON.parseObject(new String(string_byteBuffer.array(), StandardCharsets.UTF_8), Message.class);
    }

    public static void close_socketchannel(SelectionKey key) throws IOException {
        SocketChannel socketChannel=(SocketChannel) key.channel();
        socketChannel.shutdownOutput();
        socketChannel.shutdownInput();
        socketChannel.close();
        key.cancel();
    }

}
