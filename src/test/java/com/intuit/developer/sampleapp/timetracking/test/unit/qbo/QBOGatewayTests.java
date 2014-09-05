package com.intuit.developer.sampleapp.timetracking.test.unit.qbo;

import com.intuit.developer.sampleapp.timetracking.domain.*;
import com.intuit.developer.sampleapp.timetracking.mappers.CustomerMapper;
import com.intuit.developer.sampleapp.timetracking.mappers.EmployeeMapper;
import com.intuit.developer.sampleapp.timetracking.mappers.ServiceItemMapper;
import com.intuit.developer.sampleapp.timetracking.mappers.TimeActivityMapper;
import com.intuit.developer.sampleapp.timetracking.qbo.DataServiceFactory;
import com.intuit.developer.sampleapp.timetracking.qbo.QBOGateway;
import com.intuit.developer.sampleapp.timetracking.repository.CustomerRepository;
import com.intuit.developer.sampleapp.timetracking.repository.EmployeeRepository;
import com.intuit.developer.sampleapp.timetracking.repository.ServiceItemRepository;
import com.intuit.developer.sampleapp.timetracking.repository.TimeActivityRepository;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.data.Account;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.joda.money.Money;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 8/21/14
 * Time: 10:02 AM
 */
@RunWith(JMockit.class)
public class QBOGatewayTests {

    public static final String EXPECTED_INCOME_ACCOUNT_QUERY = "select * from account where accounttype = 'Income' and accountsubtype = 'ServiceFeeIncome'";
    @Tested
    QBOGateway qboDataManager;

    @Injectable
    DataServiceFactory dataServiceFactory;

    @Mocked
    DataService dataService;


    @Test
    public void testCreateEmployeeInQBO_NoMatchFound(@Mocked final EmployeeMapper mapper,
                                                     @Injectable final EmployeeRepository repository,
                                                     @Mocked final QueryResult queryResult) throws Exception {

        final String firstName = "First";
        final String lastName = "Last";
        final Employee employee = new Employee(firstName, lastName, "first.last@gmail.com", "916-222-3333");

        final Company c = new Company();
        c.setName("The Federalists");
        c.addEmployee(employee);

        final String expectedQBOId = "987654321";
        final com.intuit.ipp.data.Employee returnedQboObject = new com.intuit.ipp.data.Employee();
        returnedQboObject.setId(expectedQBOId);

        final com.intuit.ipp.data.Employee mappedQboObject = new com.intuit.ipp.data.Employee();

        new NonStrictExpectations() {{

            dataServiceFactory.getDataService(c);
            result = dataService;

            EmployeeMapper.buildQBOObject(employee);
            result = mappedQboObject;

            dataService.executeQuery(anyString);
            result = queryResult;

            dataService.add(mappedQboObject);
            result = returnedQboObject;


        }};

        qboDataManager.createEmployeeInQBO(employee);

        final String expectedQuery = String.format(QBOGateway.EXISTING_EMPLOYEE_QUERY, firstName, lastName);

        assertEquals("qboId  was not updated", expectedQBOId, employee.getQboId());

        new Verifications() {{
            dataService.executeQuery(expectedQuery);
            dataService.add(mappedQboObject);
            repository.save(employee);
        }};
    }

    @Test
    public void testCreateEmployeeInQBO_MatchFound(@Injectable final EmployeeRepository repository) throws Exception {

        final String firstName = "First";
        final String lastName = "Last";
        final Employee employee = new Employee(firstName, lastName, "first.last@gmail.com", "916-222-3333");

        final Company c = new Company();
        c.setName("The Federalists");
        c.addEmployee(employee);

        final String expectedQBOId = "987654321";
        final com.intuit.ipp.data.Employee qboEmployee = new com.intuit.ipp.data.Employee();
        qboEmployee.setId(expectedQBOId);
        qboEmployee.setGivenName(firstName);
        qboEmployee.setFamilyName(lastName);

        // Create employee query results
        final QueryResult employeeQueryResult = new QueryResult();
        employeeQueryResult.setEntities(Arrays.asList(qboEmployee));

        new NonStrictExpectations() {{

            dataServiceFactory.getDataService(c);
            result = dataService;

            dataService.executeQuery(anyString);
            times = 1;
            result = employeeQueryResult;

        }};

        qboDataManager.createEmployeeInQBO(employee);

        new Verifications() {{
            dataService.add(withAny(new com.intuit.ipp.data.Employee()));
            times = 0;
            repository.save(employee);
        }};

        assertEquals(expectedQBOId, employee.getQboId());
    }

    @Test
    public void testCreateCustomerInQBO_NoMatchFound(@Mocked final CustomerMapper mapper,
                                                     @Injectable final CustomerRepository repository,
                                                     @Mocked final QueryResult queryResult) throws Exception {

        String firstName = "First";
        String lastName = "Last";
        final Customer domainEntity = new Customer(firstName, lastName, "foo.bar@gmail.com", "916-123-4567");

        final Company c = new Company();
        c.setName("The Federalists");
        c.addCustomer(domainEntity);

        final String expectedQBOId = "987654321";
        final com.intuit.ipp.data.Customer returnedQboObject = new com.intuit.ipp.data.Customer();
        returnedQboObject.setId(expectedQBOId);

        final com.intuit.ipp.data.Customer mappedQboObject = new com.intuit.ipp.data.Customer();


        final String expectedQuery = String.format(QBOGateway.EXISTING_CUSTOMER_QUERY, firstName, lastName);

        new NonStrictExpectations() {{
            dataServiceFactory.getDataService(c);
            result = dataService;

            CustomerMapper.buildQBOObject(domainEntity);
            result = mappedQboObject;

            dataService.executeQuery(anyString);
            result = queryResult;

            dataService.add(mappedQboObject);
            result = returnedQboObject;

            repository.save(domainEntity);
        }};

        qboDataManager.createCustomerInQBO(domainEntity);

        assertEquals("qboId  was not updated", expectedQBOId, domainEntity.getQboId());

        new Verifications() {{
            dataService.executeQuery(expectedQuery);
            dataService.add(mappedQboObject);
            repository.save(domainEntity);
        }};
    }

    @Test
    public void testCreateCustomerInQBO_MatchFound(@Injectable final CustomerRepository repository) throws Exception {

        final String firstName = "First";
        final String lastName = "Last";
        final Customer customer = new Customer(firstName, lastName, "first.last@gmail.com", "916-222-3333");

        final Company c = new Company();
        c.setName("The Federalists");
        c.addCustomer(customer);

        final String expectedQBOId = "987654321";
        final com.intuit.ipp.data.Customer qboCustomer = new com.intuit.ipp.data.Customer();
        qboCustomer.setId(expectedQBOId);
        qboCustomer.setGivenName(firstName);
        qboCustomer.setFamilyName(lastName);

        // Create customer query results
        final QueryResult employeeQueryResult = new QueryResult();
        employeeQueryResult.setEntities(Arrays.asList(qboCustomer));

        new NonStrictExpectations() {{

            dataServiceFactory.getDataService(c);
            result = dataService;

            dataService.executeQuery(anyString);
            times = 1;
            result = employeeQueryResult;

        }};

        qboDataManager.createCustomerInQBO(customer);

        new Verifications() {{
            dataService.add(withAny(new com.intuit.ipp.data.Customer()));
            times = 0;
            repository.save(customer);
        }};

        assertEquals(expectedQBOId, customer.getQboId());
    }

    @Test
    public void testCreateServiceItemInQBO_NoMatchFound(@Mocked final ServiceItemMapper mapper,
                                                        @Injectable final ServiceItemRepository repository,
                                                        @Mocked final QueryResult accountQueryResult,
                                                        @Mocked final QueryResult itemQueryResult) throws Exception {

        final String name = "Research";
        final String description = "Reading a lot";
        final ServiceItem domainEntity = new ServiceItem(name, description, Money.parse("USD 50"));

        final Company c = new Company();
        c.setName("The Federalists");
        c.addServiceItem(domainEntity);

        final String expectedQBOId = "987654321";
        final com.intuit.ipp.data.Item returnedQboObject = new com.intuit.ipp.data.Item();
        returnedQboObject.setId(expectedQBOId);

        final com.intuit.ipp.data.Item mappedQboObject = new com.intuit.ipp.data.Item();

        final List<IEntity> list = new ArrayList<>();
        final Account account = new Account();
        list.add(account);
        final String expectedAccountId = "22222223";
        account.setId(expectedAccountId);

        final String expectedItemQuery = String.format(QBOGateway.EXISTING_SERVICE_ITEM_QUERY, name);

        new NonStrictExpectations() {{
            dataServiceFactory.getDataService(c);
            result = dataService;

            ServiceItemMapper.buildQBOObject(domainEntity);
            result = mappedQboObject;

            dataService.executeQuery(expectedItemQuery);
            result = itemQueryResult;

            dataService.executeQuery(EXPECTED_INCOME_ACCOUNT_QUERY);
            result = accountQueryResult;

            accountQueryResult.getEntities();
            result = list;

            dataService.add(mappedQboObject);
            result = returnedQboObject;

        }};

        qboDataManager.createItemInQBO(domainEntity);

        assertEquals("qboId  was not updated", expectedQBOId, domainEntity.getQboId());
        assertEquals("income account id was not set", expectedAccountId, mappedQboObject.getIncomeAccountRef().getValue());

        new Verifications() {{
            dataService.executeQuery(expectedItemQuery);
            dataService.add(mappedQboObject);
            repository.save(domainEntity);
        }};
    }

    @Test
    public void testCreateServiceItemInQBO_NoMatchingIncomeAccount(@Mocked final ServiceItemMapper mapper,
                                                                   @Injectable final ServiceItemRepository repository,
                                                                   @Mocked final QueryResult accountQueryResult,
                                                                   @Mocked final QueryResult itemQueryResult) throws Exception {

        final String serviceName = "serviceName";
        final ServiceItem domainEntity = new ServiceItem(serviceName, "description", Money.parse("USD 1"));

        final Company c = new Company();
        c.setName("The Federalists");
        c.addServiceItem(domainEntity);

        final com.intuit.ipp.data.Item mappedQboObject = new com.intuit.ipp.data.Item();

        final String expectedItemQuery = String.format(QBOGateway.EXISTING_SERVICE_ITEM_QUERY, serviceName);

        new NonStrictExpectations() {{
            dataServiceFactory.getDataService(c);
            result = dataService;

            ServiceItemMapper.buildQBOObject(domainEntity);
            result = mappedQboObject;

            dataService.executeQuery(expectedItemQuery);
            result = itemQueryResult;

            dataService.executeQuery(EXPECTED_INCOME_ACCOUNT_QUERY);
            result = accountQueryResult;

            accountQueryResult.getEntities();
            result = new ArrayList<IEntity>();
        }};


        boolean exceptionThrown = false;
        try {
            qboDataManager.createItemInQBO(domainEntity);
        } catch (RuntimeException e) {
            exceptionThrown = true;
            assertEquals("Could not find an account of type Income and subtype ServiceFeeIncome", e.getMessage());
        }

        assertTrue("exception not thrown", exceptionThrown);

        new Verifications() {{
            dataService.add(mappedQboObject);
            times = 0;
            repository.save(domainEntity);
            times = 0;
        }};
    }

    @Test
    public void testCreateServiceItemInQBO_MatchFound(@Injectable final ServiceItemRepository repository) throws Exception {

        final String name = "name";
        final String description = "description";
        final ServiceItem serviceItem = new ServiceItem(name, description, Money.parse("USD 1.00"));

        final Company c = new Company();
        c.setName("The Federalists");
        c.addServiceItem(serviceItem);

        final String expectedQBOId = "987654321";
        final com.intuit.ipp.data.Item qboServiceItem = new com.intuit.ipp.data.Item();
        qboServiceItem.setId(expectedQBOId);
        qboServiceItem.setName(name);

        // Create serviceItem query results
        final QueryResult employeeQueryResult = new QueryResult();
        employeeQueryResult.setEntities(Arrays.asList(qboServiceItem));

        new NonStrictExpectations() {{

            dataServiceFactory.getDataService(c);
            result = dataService;

            dataService.executeQuery(anyString);
            times = 1;
            result = employeeQueryResult;

        }};

        qboDataManager.createItemInQBO(serviceItem);

        new Verifications() {{
            dataService.add(withAny(new com.intuit.ipp.data.Item()));
            times = 0;
            repository.save(serviceItem);
        }};

        assertEquals(expectedQBOId, serviceItem.getQboId());
    }


    @Test
    public void testCreateTimeActivityInQBO(@Mocked final TimeActivityMapper mapper,
                                            @Injectable final TimeActivityRepository repository) throws Exception {

        final Company c = new Company();
        c.setName("A good company");

        final TimeActivity domainEntity = new TimeActivity();
        domainEntity.setCompany(c);

        final com.intuit.ipp.data.TimeActivity mappedQboObject = new com.intuit.ipp.data.TimeActivity();

        final String expectedQBOId = "987654321";
        final com.intuit.ipp.data.TimeActivity returnedQboObject = new com.intuit.ipp.data.TimeActivity();
        returnedQboObject.setId(expectedQBOId);

        new NonStrictExpectations() {{
            dataServiceFactory.getDataService(c);
            result = dataService;

            TimeActivityMapper.buildQBOObject(domainEntity);
            result = mappedQboObject;

            dataService.add(mappedQboObject);
            result = returnedQboObject;

        }};

        qboDataManager.createTimeActivityInQBO(domainEntity);

        new Verifications() {{
            dataService.add(mappedQboObject);
            repository.save(domainEntity);
        }};

    }
}