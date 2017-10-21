package com.sheeply.onkyovolumetoast.eiscp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;

public class EiscpSerializer {
    /*
     Adapted from Tom Gutwin,
     http://tom.webarts.ca/Blog/new-blog-items/javaeiscp-integraserialcontrolprotocol
     */
    public void write(String command, DataOutputStream outputStream) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        int eiscpDataSize = command.length() + 2 ; // this is size of the eISCP command (incl. prefix)
        int eiscpMsgSize = eiscpDataSize + 1 + 16 ; // this is the total size of the eISCP message

        // 4 byte header
        sb.append("ISCP");

        // 4 byte header size
        char padding = (char)Integer.parseInt("00", 16);
        sb.append(padding);
        sb.append(padding);
        sb.append(padding);
        sb.append((char)Integer.parseInt("10", 16));

        // 4 byte message size
        sb.append(padding);
        sb.append(padding);
        sb.append(padding);
        sb.append((char)Integer.parseInt(Integer.toHexString(eiscpMsgSize), 16));

        // eiscp_version = "01";
        sb.append((char)Integer.parseInt("01", 16));

        // 3 bytes reserved
        sb.append(padding);
        sb.append(padding);
        sb.append(padding);

        // eISCP data
        // start Character
        sb.append("!");

        // unittype '1' is receiver
        sb.append(1);

        // 3 char command and param, ie. PWR01
        sb.append(command);

        // msg end - EOF
        sb.append((char)Integer.parseInt("0D", 16));

        outputStream.writeChars(sb.toString());
        outputStream.flush();
    }

    public String read(DataInputStream stream) throws IOException
    {
        byte[] buffer = new byte[64];
        int bytesRead = stream.read(buffer);
        String rawData = new String(buffer);

        if (!rawData.startsWith("ISCP") || bytesRead < 24)
        {
            throw new InvalidObjectException("Invalid data received.");
        }

        String command = rawData.substring(18, 23);
        return command;
    }
}
