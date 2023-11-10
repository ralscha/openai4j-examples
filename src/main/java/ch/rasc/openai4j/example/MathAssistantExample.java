package ch.rasc.openai4j.example;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.assistants.Assistant;
import ch.rasc.openai4j.assistants.CodeTool;
import ch.rasc.openai4j.common.SortOrder;

public class MathAssistantExample {
	public static void main(String[] args) {
		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(c -> c.apiKey(apiKey));

		var assistants = client.assistants.list();
		Assistant assistant = null;

		for (var a : assistants.data()) {
			if (a.name().equals("Math Tutor")) {
				assistant = a;
				break;
			}
		}

		if (assistant == null) {
			assistant = client.assistants.create(c -> c.name("Math Tutor").instructions(
					"You are a personal math tutor. Write and run code to answer math questions.")
					.addTools(CodeTool.of()).model("gpt-4-1106-preview"));
		}
		System.out.println(assistant);

		var thread = client.threads.create(c -> c);
		System.out.println(thread);

		var message = client.threadsMessages.create(thread.id(),
				c -> c.role("user").content(
						"I need to solve the equation `3x + 11 = 14`. Can you help me?"));
		System.out.println(message);

		final Assistant af = assistant;
		var run = client.threadsRuns.create(thread.id(),
				c -> c.assistantId(af.id()).instructions(
						"Please address the user as Jane Doe. The user has a premium account."));

		System.out.println(run);
		while (!run.status().isTerminal()) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			run = client.threadsRuns.retrieve(thread.id(), run.id());
			System.out.println(run);
		}

		System.out.println("Messages from the assistant");
		var messages = client.threadsMessages.list(thread.id(),
				p -> p.order(SortOrder.ASC).after(message.id()));
		for (var msg : messages.data()) {
			System.out.println(msg);
		}

		System.out.println("list run steps");
		var steps = client.threadsRunsSteps.list(thread.id(), run.id());
		for (var step : steps.data()) {
			System.out.println(step);
		}
	}
}
