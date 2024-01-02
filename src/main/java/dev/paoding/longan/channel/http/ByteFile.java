package dev.paoding.longan.channel.http;

public class ByteFile {
    private byte[] content;
    private String name;
    private long length;

    public long length() {
        return length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
        length = content.length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
