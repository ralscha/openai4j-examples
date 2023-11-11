package ch.rasc.openai4j.example;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.assistants.Assistant;
import ch.rasc.openai4j.assistants.RetrievalTool;
import ch.rasc.openai4j.common.SortOrder;
import ch.rasc.openai4j.files.FileCreateRequest;
import ch.rasc.openai4j.files.FileObject;
import ch.rasc.openai4j.threads.messages.ThreadMessage.MessageContentText;

public class AssistantRetrievalExample {
	public static void main(String[] args) throws IOException, InterruptedException {
		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(c -> c.apiKey(apiKey));

		// Upload file for Assistant (Winnie-the-Pooh)
		// https://www.gutenberg.org/cache/epub/67098/pg67098.txt
		FileObject file;

		try (var httpClient = HttpClient.newHttpClient()) {
			var request = java.net.http.HttpRequest.newBuilder()
					.uri(java.net.URI.create(
							"https://www.gutenberg.org/cache/epub/67098/pg67098.txt"))
					.build();
			var response = httpClient.send(request,
					java.net.http.HttpResponse.BodyHandlers.ofString());
			var body = response.body();

			Path tmpFile = Files.createTempFile("pooh", ".txt");
			Files.writeString(tmpFile, body);

			// upload file
			file = client.files.create(FileCreateRequest.forAssistants(tmpFile));
			client.files.waitForProcessing(file.id());

			Files.delete(tmpFile);
		}

		var assistants = client.assistants.list();
		Assistant assistant = null;

		for (var a : assistants.data()) {
			if (a.name().equals("DocumentAnalyzer")) {
				assistant = a;
				break;
			}
		}

		if (assistant == null) {
			assistant = client.assistants.create(c -> c.name("DocumentAnalyzer")
					.instructions("You are a analyzer and summarizer of documents")
					.addTools(RetrievalTool.of()).addFileId(file.id())
					.model("gpt-4-1106-preview"));
		}

		var thread = client.threads.create(c -> c);

		var message = client.threadsMessages.create(thread.id(),
				c -> c.role("user").content(
						"List all persons that appear in the story. Bullet points with a short description. Maximum 10 persons"));

		final Assistant af = assistant;
		var run = client.threadsRuns.create(thread.id(), c -> c.assistantId(af.id()));

		System.out.println(run.status());
		while (!run.status().isTerminal()) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			run = client.threadsRuns.retrieve(thread.id(), run.id());
			System.out.println(run.status());
		}

		var messages = client.threadsMessages.list(thread.id(),
				p -> p.order(SortOrder.ASC).after(message.id()));
		for (var msg : messages.data()) {
			var content = msg.content()[0];
			if (content instanceof MessageContentText text) {
				System.out.println(text.text().value());
			}
		}

	}
}
