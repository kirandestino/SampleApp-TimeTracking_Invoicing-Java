package timetracking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.joda.money.Money;
import org.springframework.context.ConfigurableApplicationContext;
import timetracking.domain.*;
import timetracking.repository.*;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 8/20/14
 * Time: 3:32 PM
 */
public class DataLoader {
    /**
     * Loads oauth information from oauth.json, which is expected to be in the project root
     *
     * @param context
     */
    public static void initializeData(ConfigurableApplicationContext context) {
        if (oauthInfoNeeded(context)) {
            try {
                final File file = new File("oauth.json");
                final String jsonStr = FileUtils.readFileToString(file);
                ObjectMapper mapper = new ObjectMapper();
                final JsonNode jsonNode = mapper.readTree(jsonStr);

                createAppInfo(jsonNode, context);
                createCompany(context);

            } catch (IOException e) {
                System.err.println("Failed to read oauth information from oauth.json. Please make sure oauth.json is in the root of the project directory");
                e.printStackTrace();
            }
        }
    }


    private static void createCompany(ConfigurableApplicationContext springContext) {
        final CompanyRepository repository = springContext.getBean(CompanyRepository.class);

        if(repository.count() == 0) {
            System.out.println("No company data in the app, creating data");

            Company company = new Company("Your Law Firm");
            repository.save(company);

            createEmployees(company, springContext);
            createCustomers(company, springContext);
            createServiceItems(company, springContext);
        }
    }

    private static void createServiceItems(Company company, ConfigurableApplicationContext springContext) {
        final ServiceItemRepository repository = springContext.getBean(ServiceItemRepository.class);

        final ServiceItem serviceItem1 = new ServiceItem("Research", "Reading large ponderous tomes", Money.parse("USD 50.00"));
        company.addServiceItem(serviceItem1);
        final ServiceItem serviceItem2 = new ServiceItem("Deposition", "Asking people serious questions", Money.parse("USD 100.00"));
        company.addServiceItem(serviceItem2);

        repository.save(serviceItem1);
        repository.save(serviceItem2);
    }

    private static void createCustomers(Company company, ConfigurableApplicationContext springContext) {
        final CustomerRepository repository = springContext.getBean(CustomerRepository.class);

        final Customer customer1 = new Customer("John", "Defendant", "john.defendant@innocent.com", "916-555-7777");
        company.addCustomer(customer1);

        final Customer customer2 = new Customer("Jane", "Litigious", "jane.litigious@lawsuit.com", "916-777-9999");
        company.addCustomer(customer2);

        repository.save(customer1);
        repository.save(customer2);
    }

    private static void createEmployees(Company company, ConfigurableApplicationContext springContext) {
        final EmployeeRepository repository = springContext.getBean(EmployeeRepository.class);

        final Employee employee1 = new Employee("Jackie", "Chiles", "jackie.chiles@law.com", "916-333-4444");
        company.addEmployee(employee1);
        final Employee employee2 = new Employee("Johnnie", "Cochran", "johnnie.cochran@law.com", "916-222-5555");
        company.addEmployee(employee2);

        repository.save(employee1);
        repository.save(employee2);
    }

    private static boolean oauthInfoNeeded(ConfigurableApplicationContext context) {
        AppInfoRepository appInfoRepository = context.getBean(AppInfoRepository.class);
        return appInfoRepository.count() == 0;
    }


    private static AppInfo createAppInfo(JsonNode jsonNode, ConfigurableApplicationContext context) {
        AppInfoRepository repository = context.getBean(AppInfoRepository.class);

        final JsonNode jsonAppInfo = jsonNode.get("appInfo");

        AppInfo appInfo = new AppInfo(jsonAppInfo.get("appToken").asText(),
                jsonAppInfo.get("consumerKey").asText(),
                jsonAppInfo.get("consumerSecret").asText());

        repository.save(appInfo);

        return appInfo;

    }

}
