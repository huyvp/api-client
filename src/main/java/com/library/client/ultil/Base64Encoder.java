package com.library.client.ultil;

import java.io.*;

public class Base64Encoder {
    private static final char[] pem_array = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    protected PrintStream pStream;

    protected int bytesPerAtom() {
        return 3;
    }

    protected int bytesPerLine() {
        return 57;
    }

    protected void encodeAtom(OutputStream var1, byte[] var2, int var3, int var4) throws IOException {
        byte var5;
        if (var4 == 1) {
            var5 = var2[var3];
            byte var6 = 0;
            var1.write(pem_array[var5 >>> 2 & 63]);
            var1.write(pem_array[(var5 << 4 & 48) + (var6 >>> 4 & 15)]);
            var1.write(61);
            var1.write(61);
        } else {
            byte var8;
            if (var4 == 2) {
                var5 = var2[var3];
                var8 = var2[var3 + 1];
                byte var9 = 0;
                var1.write(pem_array[var5 >>> 2 & 63]);
                var1.write(pem_array[(var5 << 4 & 48) + (var8 >>> 4 & 15)]);
                var1.write(pem_array[(var8 << 2 & 60) + (var9 >>> 6 & 3)]);
                var1.write(61);
            } else {
                var5 = var2[var3];
                var8 = var2[var3 + 1];
                byte var10 = var2[var3 + 2];
                var1.write(pem_array[var5 >>> 2 & 63]);
                var1.write(pem_array[(var5 << 4 & 48) + (var8 >>> 4 & 15)]);
                var1.write(pem_array[(var8 << 2 & 60) + (var10 >>> 6 & 3)]);
                var1.write(pem_array[var10 & 63]);
            }
        }
    }

    protected void encodeBufferPrefix(OutputStream var1) {
        this.pStream = new PrintStream(var1);
    }

    protected int readFully(InputStream var1, byte[] var2) throws IOException {
        for(int var3 = 0; var3 < var2.length; ++var3) {
            int var4 = var1.read();
            if (var4 == -1) {
                return var3;
            }

            var2[var3] = (byte)var4;
        }

        return var2.length;
    }

    public void encode(InputStream var1, OutputStream var2) throws IOException {
        byte[] var5 = new byte[this.bytesPerLine()];
        this.encodeBufferPrefix(var2);

        while(true) {
            int var4 = this.readFully(var1, var5);
            if (var4 == 0) {
                break;
            }

            for(int var3 = 0; var3 < var4; var3 += this.bytesPerAtom()) {
                if (var3 + this.bytesPerAtom() <= var4) {
                    this.encodeAtom(var2, var5, var3, this.bytesPerAtom());
                } else {
                    this.encodeAtom(var2, var5, var3, var4 - var3);
                }
            }

            if (var4 < this.bytesPerLine()) {
                break;
            }
        }
    }

    public String encode(byte[] var1) {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1);
        String var4;

        try {
            this.encode(var3, var2);
            var4 = var2.toString("8859_1");
            return var4;
        } catch (Exception var6) {
            throw new Error("CharacterEncoder.encode internal error");
        }
    }
}
