/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 *
 * @author jernej
 */
public class IRwaver {

   
    /**
     * This script produces RC5 IR codes in wav format
     *
     * I use script directly from netbenas, but I added args so you can run it
     * from command line; in that case, first param is the command,
     *  2nd is the address
     *  3rd is the frequency
     * 
     * java -jar iRwaver.jar 20 0 38
     * would create command 0x14 on 38kz frequence
     *
     
     */
    public static void main(String[] args) {
        int command=0x11;    //command we are transmitting
        int address=0b00000; //address of device we are transmitting to
        String kHz="36";
        
        if(args.length>=1){
            command=Integer.parseInt(args[0]);
        }
        if(args.length>1){
            address=Integer.parseInt(args[1]);
        }
        
        if(args.length>2){
            kHz=args[2];
        }
        
        System.out.println(System.getProperty("user.dir"));
        try {        
            Path path = Paths.get(System.getProperty("user.dir")+"/mark"+kHz+"kHz.dat");
            byte[] mark = Files.readAllBytes(path);
            
            path = Paths.get(System.getProperty("user.dir")+"/space"+kHz+"kHz.dat");
            byte[] space = Files.readAllBytes(path);
            
            byte[] out= new byte[0];
            
            //RC5 header bit, always 1
            out=concat(out,space);
            out=concat(out,mark);
            
            //RC5 low field bit, 1= low, 0-63 decimal, 0=high, 64-127 decimal
            out=concat(out,space);
            out=concat(out,mark);
            
             //RC5 toggle bit, are we repeating or sending first time, don't know which is which
            out=concat(out,mark);
            out=concat(out,space);

            
            out = byteToRc5(address, 5, out, space, mark);
            out = byteToRc5(command, 6, out, space, mark);
            
            save(System.getProperty("user.dir")+"/rc5_0x"+Integer.toHexString(command)+"_"+kHz+"kHz.wav", out);
            
        } catch (IOException ex) {
            Logger.getLogger(IRwaver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static byte[] byteToRc5(int data, int len, byte[] out, byte[] space, byte[] mark) {
        int _byte=data << 8-len; // cut insignificant bits
        for(int j=0;j<len;j++)
        {
            int bit=_byte & 0b10000000;
            if(bit==0b10000000){
                out=concat(out,space);
                out=concat(out,mark);
            }
            else
            {
                out=concat(out,mark); 
                out=concat(out,space);
            }
            _byte= _byte << 1;
        }
        return out;
    }
    
    public static void save(String filename, byte[] input){
        System.out.println(filename);
        // assumes 44,100 samples per second
        // use 16-bit audio, mono, signed PCM, little Endian
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        
        // now save the file
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(input);
            AudioInputStream ais = new AudioInputStream(bais, format, input.length/2);
            if (filename.endsWith(".wav") || filename.endsWith(".WAV")) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filename));
            }
        }
        catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }
    
    public static byte[] concat(byte[] A,  byte[] B) {
       int aLen = A.length;
       int bLen = B.length;
       byte[] C= new byte[aLen+bLen];
       System.arraycopy(A, 0, C, 0, aLen);
       System.arraycopy(B, 0, C, aLen, bLen);
       return C;
   }
}
