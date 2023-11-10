package ch.rasc.openai4j.example;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.audio.AudioSpeechRequest;
import feign.Response;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AudioExample {

    public static void main(String[] args) {
        String apiKey = Util.getApiKey();
        var client = OpenAIClient.create(c -> c.apiKey(apiKey));

        var input = "Wie geht es dir?";

        try (Response response =
                     client.audio.create(
                             r ->
                                     r.input(input)
                                             .model(AudioSpeechRequest.Model.TTS_1_HD)
                                             .voice(AudioSpeechRequest.Voice.ALLOY));
             FileOutputStream fos = new FileOutputStream("hello.mp3");
             var body = response.body();
             var is = body.asInputStream()) {
            is.transferTo(fos);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Path inp = Paths.get("hello.mp3");
        var resp = client.audio.transcriptionsCreate(r -> r.file(inp));
        System.out.println(resp);

        var resp2 = client.audio.translationsCreate(r -> r.file(inp));
        System.out.println(resp2);
    }
}
