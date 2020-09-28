import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import javax.sound.sampled.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
* Просто записывает звук с микрофона и заливает получившиеся файлы на dropbox
*/
public class JavaSoundRecorder
{
    private String filePath;
    private final AudioFileFormat.Type fileType;
    private TargetDataLine line;
    private final AudioFormat format;
    private final DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
    private final String ACCESS_TOKEN = "fhmYJWLf-FcAAAAAAAAAAU0fApQ8_Up0CoasHzsqzAnyRA-tgS98K1QD2YgHdHzI";
    private final DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

    public JavaSoundRecorder()
    {
        fileType = AudioFileFormat.Type.WAVE;
        format = new AudioFormat(16000, 8, 2, true, true);
    }

    public void start(File file)
    {
        Thread thread = new Thread(() -> {
            try {
                DataLine.Info info =
                    new DataLine.Info(TargetDataLine.class, format);
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                System.out.println("Start recording...");
                AudioInputStream ais = new AudioInputStream(line);
                AudioSystem.write(ais, fileType, file);
            }
            catch (Exception ex) {
                System.out.println("Record error!");
                ex.printStackTrace();
            }
        });
        thread.start();
    }

    public void stop(File file, int seconds)
    {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000 * seconds);
                System.out.println("Recording stopped...");
            } catch (InterruptedException e) {
                System.out.println("Stop recording error");
            }
            line.stop();
            line.close();

            loadToDropBox(file);
            if (file.delete())
                System.out.println("File deleted");
            recordAudio(seconds);
        });
        thread.start();
    }

    public void loadToDropBox(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            client.files().uploadBuilder(String.format("/%s", filePath)).uploadAndFinish(inputStream);
            System.out.println("Uploaded on DropBox");
        } catch (Exception e) {
            System.out.println("Upload error to DropBox");
        }
    }

    public void recordAudio(int seconds)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'-'HHmmss");
        filePath = sdf.format(new Date()) + ".wav";
        File file = new File(filePath);
        start(file);
        stop(file, seconds);
    }
}
