package VaadinDash;

import VaadinDash.entity.DashCounter;
import VaadinDash.repository.CounterRepository;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WebBrowser;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.servlet.annotation.WebServlet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SpringUI
@Theme("mytheme")
public class VaadinDashboardUI extends UI {

    @Autowired
    private CounterRepository repository;

    VerticalLayout layout;
    HorizontalLayout layoutInformers;
    HorizontalLayout layoutFooter;



    @Override
    protected void init(VaadinRequest vaadinRequest) {

        setupLayout();
        showHeader();
        showPanels();
        showFooter();
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = VaadinDashboardUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }



    private void setupLayout() {
        layout = new VerticalLayout();
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        layout.setMargin(true);
        layout.setSpacing(true);

        setContent(layout);
    }


    private void showHeader() {

        Label header = new Label("Тестовое сетевое приложение");
        header.addStyleName(ValoTheme.LABEL_H2);
        layout.addComponent(header);

    }


    private void showPanels() {

        layoutInformers = new HorizontalLayout();
        layoutInformers.setWidth(50, Unit.PERCENTAGE);
        layoutInformers.setHeight(50, Unit.PERCENTAGE);
      //  layoutInformers.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        layout.addComponent(layoutInformers);

        showWeatherPanel();
        showCurrencyPanel();
        showCounterPanel();

    }

    private void showWeatherPanel() {

        Panel panelWeather = new Panel("Погода");
        panelWeather.setHeight(225, Unit.PIXELS);
        panelWeather.setIcon(VaadinIcons.CLOUD);
        panelWeather.addStyleName(ValoTheme.PANEL_WELL);

        VerticalLayout weatherLayout = new VerticalLayout();
        weatherLayout.setWidth(100, Unit.PERCENTAGE);
        weatherLayout.setHeight(100, Unit.PERCENTAGE);
        weatherLayout.setSpacing(true);


        List<String> cities = new ArrayList<>();
        cities.add("Москва");
        cities.add("Санкт-Петербург");
        cities.add("Новосибирск");

        ComboBox changeCity = new ComboBox<>("Выберите город:", cities);

        changeCity.setTextInputAllowed(false);
        changeCity.setEmptySelectionAllowed(false);
        changeCity.setSelectedItem(cities.get(2));
        weatherLayout.addComponent(changeCity);

        Label weather = new Label();
        weather.setCaption("");
        weather.setValue("Текущая погода: "+getWeather());
        weatherLayout.addComponent(weather);

        Button buttonRefresh = new Button("Обновить");
        buttonRefresh.setStyleName(ValoTheme.BUTTON_PRIMARY);
        weatherLayout.addComponent(buttonRefresh);

        panelWeather.setContent(weatherLayout);
        layoutInformers.addComponent(panelWeather);
    }

    private void showCurrencyPanel() {

        Panel panelCurrency = new Panel("Курсы валют");
        panelCurrency.setHeight(225, Unit.PIXELS);
        panelCurrency.setIcon(VaadinIcons.MONEY_EXCHANGE);
        panelCurrency.addStyleName(ValoTheme.PANEL_WELL);

        VerticalLayout currencyLayout = new VerticalLayout();
        currencyLayout.setWidth(100, Unit.PERCENTAGE);
        currencyLayout.setHeight(100, Unit.PERCENTAGE);
        currencyLayout.setSpacing(false);

        HashMap<String,Double> currencies = new HashMap<String,Double>();
        currencies = getCurrency();

        currencyLayout.addComponent(new Label("USD: "+currencies.get("usd").toString()));
        currencyLayout.addComponent(new Label("EUR: "+currencies.get("eur").toString()));

        Button buttonRefresh = new Button("Обновить");
        buttonRefresh.setStyleName(ValoTheme.BUTTON_PRIMARY);
        currencyLayout.addComponent(buttonRefresh);

        panelCurrency.setContent(currencyLayout);
        layoutInformers.addComponent(panelCurrency);

    }

    private void showCounterPanel() {

        Panel panelCounter = new Panel("Счетчик посещений");
        panelCounter.setHeight(225, Unit.PIXELS);
        panelCounter.setIcon(VaadinIcons.USER);
        panelCounter.addStyleName(ValoTheme.PANEL_WELL);

        VerticalLayout weatherLayout = new VerticalLayout();
        weatherLayout.setWidth(100, Unit.PERCENTAGE);
        weatherLayout.setHeight(100, Unit.PERCENTAGE);
        weatherLayout.setSpacing(false);

        weatherLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        Label showVisits = new Label(getCounter());
        showVisits.addStyleName(ValoTheme.LABEL_H1);

        weatherLayout.addComponent(showVisits);

        panelCounter.setContent(weatherLayout);

        layoutInformers.addComponent(panelCounter);

    }

    private void showFooter() {

        layoutFooter = new HorizontalLayout();
        layoutFooter.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        layout.addComponent(layoutFooter);
        showDate();
        showIP();
    }


    private void showDate() {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String datetime = formatter.format(date);

        Label datelabel = new Label("Информация по состоянию на: "+datetime);
        datelabel.addStyleName(ValoTheme.LABEL_BOLD);
        layoutFooter.addComponent(datelabel);

    }

    private void showIP() {

            WebBrowser webBrowser = Page.getCurrent().getWebBrowser();
            Label ipAddresslabel = new Label("Ваш IP: "+webBrowser.getAddress());
            ipAddresslabel.addStyleName(ValoTheme.LABEL_BOLD);
            layoutFooter.addComponent(ipAddresslabel);

        }


        private String getWeather ()  {

            String weather="";

            final String url = "http://api.openweathermap.org/data/2.5/weather?id=1496747&appid=965e6e466b00aaf6586cffb216a4c695";

            RestTemplate template = new RestTemplate(); // инициализация REST-шаблона

            template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());     // указываем конвертер для шаблона
            // отвечает за то, каким образом JSON-объект будет преобразован в JAVA-объект:

            JSONObject json = template.getForObject(url, JSONObject.class);     // получаем JSON-объект и передаем его в CosmosDTO класс
            // возвращает сконвертированный JAVA-объект

            HashMap<String,String> hmap = (HashMap<String,String>) json.get("main");

            Object obj = hmap.get("temp");

            Double temp = (Double) obj - 273.15;

            weather = temp.toString();


            return weather;

        }

         private HashMap<String,Double> getCurrency() {

            HashMap<String,Double> curr = new HashMap<String,Double>();

            final String usd = "http://api.fixer.io/latest?base=USD";
            final String eur = "http://api.fixer.io/latest?base=EUR";

            RestTemplate template = new RestTemplate(); // инициализация REST-шаблона

            template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());     // указываем конвертер для шаблона
            // отвечает за то, каким образом JSON-объект будет преобразован в JAVA-объект:

            // Получаем USD

            JSONObject json = template.getForObject(usd, JSONObject.class);     // получаем JSON-объект и передаем его в CosmosDTO класс
            // возвращает сконвертированный JAVA-объект

            HashMap<String,String> hmap = (HashMap<String,String>) json.get("rates");

            Object obj = hmap.get("RUB");

            Double usdrate = (Double) obj;

            curr.put("usd",usdrate);

            hmap.clear();

            // Получаем EUR

            json = template.getForObject(eur, JSONObject.class);     // получаем JSON-объект и передаем его в CosmosDTO класс
            // возвращает сконвертированный JAVA-объект

            hmap = (HashMap<String,String>) json.get("rates");

            obj = hmap.get("RUB");

            Double eurrate = (Double) obj;

            curr.put("eur",eurrate);


            return curr;
        }


        private String getCounter() {

            DashCounter count = new DashCounter();

            count = repository.findAll().get(0);

            count.setCount(count.getCount()+1L);

            repository.save(count);

            return String.valueOf(count.getCount());

        }



}

// http://api.openweathermap.org/data/2.5/weather?id=1496747&appid=965e6e466b00aaf6586cffb216a4c695