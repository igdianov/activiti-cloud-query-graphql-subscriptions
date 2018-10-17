package org.activiti.cloud.query.qraphql.subscriptions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.stagemonitor.core.Stagemonitor;

@SpringBootApplication
public class GraphqlSubscriptionsApplication {

	public static void main(String[] args) {
		Stagemonitor.init();
		
		SpringApplication.run(GraphqlSubscriptionsApplication.class, args);
	}
}
