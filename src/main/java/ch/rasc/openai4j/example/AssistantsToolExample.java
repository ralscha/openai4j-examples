package ch.rasc.openai4j.example;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ch.rasc.openai4j.Configuration;
import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.assistants.CodeTool;
import ch.rasc.openai4j.common.SortOrder;
import ch.rasc.openai4j.threads.messages.ThreadMessage.MessageContentText;

public class AssistantsToolExample {
	public static void main(String[] args) {
		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(Configuration.builder().apiKey(apiKey).build());

		var assistants = client.assistants.list();
		for (var a : assistants.data()) {
			client.assistants.delete(a.id());
		}

		// Without the Code tool
		var assistant = client.assistants.create(r -> r.name("Math Tutor").instructions(
				"You are a personal math tutor. Answer questions briefly, in a sentence or less.")
				.model("gpt-4-1106-preview"));

		String userMessage = "Can you give me the solution of 3 * 3 + 2 * 2 - 1?";
		var thread = client.threads.create();
		client.threadsMessages.create(thread.id(), r -> r.content(userMessage));

		var run = client.threadsRuns.create(thread.id(),
				r -> r.assistantId(assistant.id()));
		client.threadsRuns.waitForProcessing(run, 10, TimeUnit.SECONDS, 1,
				TimeUnit.MINUTES);

		var messages = client.threadsMessages.list(thread.id(),
				r -> r.order(SortOrder.ASC));
		prettyPrint(messages.data());

		// With the Code tool
		client.assistants.update(assistant.id(), r -> r.addTools(CodeTool.of()));

		run = client.threads.createAndRun(r -> r.assistantId(assistant.id())
				.thread(t -> t.userRole().content(userMessage)));

		client.threadsRuns.waitForProcessing(run, 10, TimeUnit.SECONDS, 1,
				TimeUnit.MINUTES);

		messages = client.threadsMessages.list(run.threadId(),
				r -> r.order(SortOrder.ASC));
		prettyPrint(messages.data());

	}

	private static void prettyPrint(
			List<ch.rasc.openai4j.threads.messages.ThreadMessage> messages) {
		System.out.println("# Messages");
		for (var msg : messages) {
			var content = msg.content().get(0);
			if (content instanceof MessageContentText text) {
				System.out.println(msg.role() + ": " + text.text().value());
			}
		}
		System.out.println();
	}
}
