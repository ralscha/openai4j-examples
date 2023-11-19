package ch.rasc.openai4j.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.finetuningjobs.FineTuningJobCreateRequest;

public class FineTuningJobExample {
	public static void main(String[] args) throws JsonProcessingException {
		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(c -> c.apiKey(apiKey));

		ObjectMapper om = new ObjectMapper();
		var c = FineTuningJobCreateRequest.builder().model("test").validationFile("val")
				.trainingFile("train")
				.hyperparameters(
						h -> h.batchSize(1).learningRateMultiplier(2).nEpochs(3))
				.build();
		System.out.println(om.writeValueAsString(c));
		var jobs = client.fineTuningJobs.list();
		for (var job : jobs.data()) {
			System.out.println(job);

			var j = client.fineTuningJobs.retrieve(job.id());
			System.out.println(j.id());

			var events = client.fineTuningJobs.listEvents(job.id());
			for (var event : events.data()) {
				System.out.println(event);
			}

			// var c = client.cancel(job.id());
			// System.out.println(c);
		}
	}
}
