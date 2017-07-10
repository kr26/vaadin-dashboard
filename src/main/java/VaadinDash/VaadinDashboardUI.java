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

    VerticalLayout layout; // основной макет
    HorizontalLayout layoutInformers; // макет информеров
    VerticalLayout layoutFooter; // макет подвала


    @Override
    protected void init(VaadinRequest vaadinRequest) {

        // поочереди запускаем методы отображения макетов с содержимым

        setupLayout();
        showHeader();
        showPanels();
        showFooter();
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = VaadinDashboardUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }


    private void setupLayout() {     // конфигурация основного макета
        layout = new VerticalLayout();
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        layout.setMargin(true);
        layout.setSpacing(true);

        setContent(layout);
    }


    private void showHeader() { // шапка

        Label header = new Label("Тестовое сетевое приложение");
        header.addStyleName(ValoTheme.LABEL_H2);
        layout.addComponent(header);

    }


    private void showPanels() { // панели отображаются в layoutInformers

        layoutInformers = new HorizontalLayout();
        layoutInformers.setWidth(50, Unit.PERCENTAGE);
        layoutInformers.setHeight(50, Unit.PERCENTAGE);

        layout.addComponent(layoutInformers);

        // вызываем подгрузку панелей
        showWeatherPanel();
        showCurrencyPanel();
        showCounterPanel();

    }

    private void showWeatherPanel() {

        // создаем панель
        Panel panelWeather = new Panel("Погода");
        panelWeather.setHeight(230, Unit.PIXELS);
        panelWeather.setIcon(VaadinIcons.CLOUD);
        panelWeather.addStyleName(ValoTheme.PANEL_WELL);

        // панель содержит weatherLayout
        VerticalLayout weatherLayout = new VerticalLayout();
        weatherLayout.setWidth(100, Unit.PERCENTAGE);
        weatherLayout.setHeight(100, Unit.PERCENTAGE);
        weatherLayout.setMargin(true);
        weatherLayout.setSpacing(true);

        // список городов
        List<String> cities = new ArrayList<>();
        cities.add("Москва");
        cities.add("Санкт-Петербург");
        cities.add("Новосибирск");

        // выводим в комбобокс
        ComboBox changeCity = new ComboBox<>("Выберите город:", cities);
        changeCity.setTextInputAllowed(false);
        changeCity.setEmptySelectionAllowed(false);
        changeCity.setSelectedItem(cities.get(2));

        // выводим текущую погоду в label
        Label weather = new Label();
        weather.setCaption("");
        // вызов функции подгрузки данных по API
        weather.setValue("Текущая погода: "+getWeather(changeCity.getValue().toString()));

        // выводим погоду на завтра в label
        Label forecast = new Label();
        // вызов функции подгрузки данных по API
        forecast.setValue("Погода на завтра: "+getForecast(changeCity.getValue().toString()));

        // а также при смене города тоже сразу подгружаем соответстующие данные
        changeCity.addValueChangeListener(event ->
        {
            weather.setValue("Текущая погода: "+getWeather(changeCity.getValue().toString()));
            forecast.setValue("Погода на завтра: "+getForecast(changeCity.getValue().toString()));
        });


        // кнопка Обновить
        Button buttonRefresh = new Button("Обновить");
        buttonRefresh.setStyleName(ValoTheme.BUTTON_PRIMARY);

        // вызов функции подгрузки данных по API
        buttonRefresh.addClickListener
                (event ->
                {
                    weather.setValue("Текущая погода: "+getWeather(changeCity.getValue().toString()));
                    forecast.setValue("Погода на завтра: "+getForecast(changeCity.getValue().toString()));
                });


        // добавляем на макет
        weatherLayout.addComponents(changeCity, weather, forecast, buttonRefresh);

        panelWeather.setContent(weatherLayout);
        layoutInformers.addComponent(panelWeather);
    }

    private void showCurrencyPanel() {

        // создаем панель
        Panel panelCurrency = new Panel("Курсы валют");
        panelCurrency.setHeight(230, Unit.PIXELS);
        panelCurrency.setIcon(VaadinIcons.MONEY_EXCHANGE);
        panelCurrency.addStyleName(ValoTheme.PANEL_WELL);

        // панель содержит currencyLayout
        VerticalLayout currencyLayout = new VerticalLayout();
        currencyLayout.setWidth(100, Unit.PERCENTAGE);
        currencyLayout.setHeight(100, Unit.PERCENTAGE);
        currencyLayout.setSpacing(false);

        // разные валюты, поэтому значения в Map
        HashMap<String,Double> currencies;

        // подгружаем данные по API
        currencies = getCurrency();

        // выводим курсы валют в label
        Label usdLabel = new Label();
        Label eurLabel = new Label();
        usdLabel.setValue("USD: "+currencies.get("usd").toString());
        eurLabel.setValue("EUR: "+currencies.get("eur").toString());

        // кнопка Обновить
        Button buttonRefresh = new Button("Обновить");
        buttonRefresh.setStyleName(ValoTheme.BUTTON_PRIMARY);
        // вызов функции подгрузки данных по API
        buttonRefresh.addClickListener
                (event ->
                {
                    usdLabel.setValue("USD: "+currencies.get("usd").toString());
                    eurLabel.setValue("EUR: "+currencies.get("eur").toString());
                });

        // добавляем на макет
        currencyLayout.addComponents(usdLabel, eurLabel, buttonRefresh);

        panelCurrency.setContent(currencyLayout);
        layoutInformers.addComponent(panelCurrency);

    }

    private void showCounterPanel() {

        // создаем панель
        Panel panelCounter = new Panel("Счетчик посещений");
        panelCounter.setHeight(230, Unit.PIXELS);
        panelCounter.setIcon(VaadinIcons.USER);
        panelCounter.addStyleName(ValoTheme.PANEL_WELL);

        // панель содержит counterLayout
        VerticalLayout counterLayout = new VerticalLayout();
        counterLayout.setWidth(100, Unit.PERCENTAGE);
        counterLayout.setHeight(100, Unit.PERCENTAGE);
        counterLayout.setSpacing(false);

        // выравнивание по центру
        counterLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        // счетчик посещений в label
        Label showVisits = new Label(getCounter());
        showVisits.addStyleName(ValoTheme.LABEL_H1);

        // добавляем на макет
        counterLayout.addComponent(showVisits);

        panelCounter.setContent(counterLayout);

        layoutInformers.addComponent(panelCounter);

    }

    private void showFooter() { // отображаем подвал

        layoutFooter = new VerticalLayout();
        layoutFooter.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        layout.addComponent(layoutFooter);
        showDate(); // отображаем дату
        showIP(); // отображаем IP
    }


    private void showDate() {

        // дата в нужном формате
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String datetime = formatter.format(date);

        Label datelabel = new Label("Информация по состоянию на: "+datetime);
        datelabel.addStyleName(ValoTheme.LABEL_BOLD);
        layoutFooter.addComponent(datelabel);

    }

    private void showIP() {

        // вывод IP
        WebBrowser webBrowser = Page.getCurrent().getWebBrowser();
        Label ipAddresslabel = new Label("Ваш IP: "+webBrowser.getAddress());
        ipAddresslabel.addStyleName(ValoTheme.LABEL_BOLD);
        layoutFooter.addComponent(ipAddresslabel);

        }


        private String getWeather (String city) { // запрос текущей погоды по API

            String weather="";

            String url="";

            switch (city) { // в зависимости от города URL запроса будет разный
                case "Москва": url = "http://api.openweathermap.org/data/2.5/weather?id=524901&appid=965e6e466b00aaf6586cffb216a4c695";
                    break;

                case "Санкт-Петербург": url = "http://api.openweathermap.org/data/2.5/weather?id=498817&appid=965e6e466b00aaf6586cffb216a4c695";
                    break;

                case "Новосибирск": url = "http://api.openweathermap.org/data/2.5/weather?id=1496747&appid=965e6e466b00aaf6586cffb216a4c695";
                    break;

                    default: url = "http://api.openweathermap.org/data/2.5/weather?id=1496747&appid=965e6e466b00aaf6586cffb216a4c695";

            }

            try {

                RestTemplate template = new RestTemplate(); // инициализация REST-шаблона

                template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());     // указываем конвертер для шаблона
                // отвечает за то, каким образом JSON-объект будет преобразован в JAVA-объект:

                JSONObject json = template.getForObject(url, JSONObject.class);     // получаем JSON-объект и передаем его в CosmosDTO класс
                // возвращает сконвертированный JAVA-объект

                // вскрываем JSON

                HashMap<String, String> hmap = (HashMap<String, String>) json.get("main");

                Object obj = hmap.get("temp");

                // переводим из Кельвинов в Цельсии
                Double temp = (Double) obj - 273.15;

                // вывод с одной цифрой после запятой
                weather = String.format("%.1f", temp);

            }
            catch (Exception e) // если загрузка данных не удалась
            {
                Notification.show("Нет данных о погоде");
                weather = "Нет данных";
            }

            return weather;

        }


    private String getForecast (String city)  { // запрос прогноза погоды на завтра по API

        String forecast="";

        String url="";

        switch (city) { // в зависимости от города URL запроса будет разный
            case "Москва": url = "http://api.openweathermap.org/data/2.5/forecast/daily?id=524901&appid=965e6e466b00aaf6586cffb216a4c695";
                break;

            case "Санкт-Петербург": url = "http://api.openweathermap.org/data/2.5/forecast/daily?id=498817&appid=965e6e466b00aaf6586cffb216a4c695";
                break;

            case "Новосибирск": url = "http://api.openweathermap.org/data/2.5/forecast/daily?id=1496747&appid=965e6e466b00aaf6586cffb216a4c695";
                break;

            default: url = "http://api.openweathermap.org/data/2.5/forecast/daily?id=1496747&appid=965e6e466b00aaf6586cffb216a4c695";

        }

        try {

            RestTemplate template = new RestTemplate(); // инициализация REST-шаблона

            template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());     // указываем конвертер для шаблона
            // отвечает за то, каким образом JSON-объект будет преобразован в JAVA-объект:

            JSONObject json = template.getForObject(url, JSONObject.class);     // получаем JSON-объект и передаем его в CosmosDTO класс
            // возвращает сконвертированный JAVA-объект

            // вскрываем JSON

            ArrayList<Object> list = (ArrayList) json.get("list");

            HashMap<Object, Object> hmap = (HashMap<Object, Object>) list.get(1);

            Object obj = hmap.get("temp");

            HashMap<String, Double> values = (HashMap<String, Double>) obj;

            Object degree = values.get("day");

            // переводим из Кельвинов в Цельсии

            Double temp = (Double) degree - 273.15;

            // вывод с одной цифрой после запятой

            forecast = String.format("%.1f", temp);

        }
        catch (Exception e) // если загрузка данных не удалась
        {
            Notification.show("Нет данных о погоде");
            forecast = "Нет данных";
        }

        return forecast;

    }


         private HashMap<String,Double> getCurrency() { // запрос курсов валют по API

            HashMap<String,Double> curr = new HashMap<>();

             // URL запроса для разных валют

            final String usd = "http://api.fixer.io/latest?base=USD";
            final String eur = "http://api.fixer.io/latest?base=EUR";

             try {

                 RestTemplate template = new RestTemplate(); // инициализация REST-шаблона

                 template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());     // указываем конвертер для шаблона
                 // отвечает за то, каким образом JSON-объект будет преобразован в JAVA-объект:

                 // получаем USD

                 JSONObject json = template.getForObject(usd, JSONObject.class);     // получаем JSON-объект и передаем его в CosmosDTO класс
                 // возвращает сконвертированный JAVA-объект

                 // вскрываем JSON

                 HashMap<String, String> hmap = (HashMap<String, String>) json.get("rates");

                 Object obj = hmap.get("RUB");

                 Double usdrate = (Double) obj;

                 curr.put("usd", usdrate);

                 hmap.clear();

                 // получаем EUR

                 json = template.getForObject(eur, JSONObject.class);     // получаем JSON-объект и передаем его в CosmosDTO класс
                 // возвращает сконвертированный JAVA-объект

                 hmap = (HashMap<String, String>) json.get("rates");

                 obj = hmap.get("RUB");

                 Double eurrate = (Double) obj;

                 curr.put("eur", eurrate);
             }
             catch (Exception e) // если загрузка данных не удалась
             {
                 Notification.show("Нет данных о курсах валют");
                 curr.put("usd", 0.0);
                 curr.put("eur", 0.0);
             }

            return curr;
        }


        private String getCounter() { // получение и запись счетчика через репозиторий

            DashCounter count = new DashCounter();

            try {

                // работаем с репозиторием Mongo
                count = repository.findAll().get(0);

                // инкремент
                count.setCount(count.getCount() + 1L);

                // сохраняем
                repository.save(count);

            }
            catch (Exception e) // если проблема с БД
            {
                Notification.show("Нет соединения с базой данных");
                count.setCount(0L);
            }

            return String.valueOf(count.getCount());

        }



}
