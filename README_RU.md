# Smart Home Simulator

**Language / Язык / Jazyk:**
[🇬🇧 English](README.md) | [🇷🇺 Русский](README_RU.md) | [🇨🇿 Čeština](README_CZ.md)

---

Симуляция умного дома на Java с пиксельным десктопным визуализатором. Проект моделирует жизнь чешской семьи в умном доме - жители взаимодействуют с устройствами, следуют распорядку дня, реагируют на погоду, управляют запасами еды. Вся симуляция воспроизводится в реальном времени через GUI в стиле пошаговых стратегий.

---

## Содержание

- [Технологический стек](#технологический-стек)
- [Архитектура системы](#архитектура-системы)
- [Паттерны проектирования](#паттерны-проектирования)
- [Иерархия классов](#иерархия-классов)
- [Жители и поведение](#жители-и-поведение)
- [Система устройств](#система-устройств)
- [Система еды и шопинга](#система-еды-и-шопинга)
- [Система погоды](#система-погоды)
- [Система энергии](#система-энергии)
- [Визуализатор](#визуализатор)
- [Отчёты](#отчёты)
- [Запуск проекта](#запуск-проекта)
- [Конфигурация](#конфигурация)
- [Структура проекта](#структура-проекта)

---

## Технологический стек

| Технология | Версия | Назначение |
|---|---|---|
| Java | 23 | Основной язык |
| Java Swing | встроен в JDK | Десктопный GUI визуализатор |
| Jackson Databind | 2.17.1 | Парсинг JSON конфигурации (`house.json`) |
| Maven | 3.x | Система сборки |

---

## Архитектура системы

### Диаграмма 1 - Запуск и инициализация

![Диаграмма 1 - Запуск и инициализация](docs/diagrams/diagram1_Startup_and_Initialization.png)

### Диаграмма 2 - Движок симуляции и цикл тиков

![Диаграмма 2 - Движок симуляции](docs/diagrams/diagram2_Simulation_Engine_and_Tick_Cycle.png)

### Диаграмма 3 - Система событий (Observer + Chain of Responsibility)

![Диаграмма 3 - Система событий](docs/diagrams/diagram3_Event_System__Observer___Chain_of_Responsibility_.png)

---

## Паттерны проектирования

### State - состояния устройств

Устройство делегирует всё поведение текущему состоянию. При смене состояния объект состояния заменяется целиком.

```java
// Device.java
private DeviceState state = new OffState();

public void turnOn()  { state.turnOn(this);  }
public void turnOff() { state.turnOff(this); }
public void tick()    { state.tick(this);    }
```

| Состояние | turnOn | turnOff | tick |
|---|---|---|---|
| `OffState` | переходит в OnState | ничего | ничего |
| `OnState` | ничего | переходит в OffState | случайная поломка |
| `BrokenState` | заблокировано | заблокировано | ждёт ремонта |

### Observer - шина событий

`EventBus` полностью развязывает устройства и жителей. Устройство не знает о существовании конкретных классов жителей.

```java
// При инициализации
eventBus.subscribe(father);
eventBus.subscribe(mother);
eventBus.subscribe(new SystemEventListener(chain, ctx));

// При поломке устройства
device.publishEvent(new Event(EventType.DEVICE_BROKEN, device, user));
// EventBus вызывает onEvent() у всех подписчиков
```

### Chain of Responsibility - обработка событий

События проходят по цепочке обработчиков. Каждый либо обрабатывает событие, либо передаёт дальше.

```java
father.setNext(mother);
mother.setNext(daughter);
daughter.setNext(grandfather);
grandfather.setNext(fallback); // catch-all
```

### Singleton - единый контекст симуляции

`SmartHomeContext` гарантирует что все компоненты системы работают с одним и тем же состоянием дома.

```java
public static synchronized SmartHomeContext getInstance() {
    if (instance == null) instance = new SmartHomeContext();
    return instance;
}
```

`synchronized` обеспечивает thread safety - без него при одновременном вызове из двух потоков могло бы создаться два разных экземпляра контекста.

### Factory - создание объектов

Фабрики инкапсулируют логику создания. `SmartHomeContext` никогда не вызывает `new Father()` напрямую.

```java
Person person = switch (def.role) {
    case FATHER      -> new Father(def.id, def.name, def.role, location, ps);
    case MOTHER      -> new Mother(def.id, def.name, def.role, location, ps);
    case SON         -> new Son(def.id, def.name, def.role, location, ps);
    case GRANDFATHER -> new Grandfather(def.id, def.name, def.role, location, ps);
    default          -> new Person(def.id, def.name, def.role, location, ps);
};
```

Добавление нового типа жителя требует только создания нового класса и одной строки в `PersonFactory`. `SmartHomeContext` и `SimulationEngine` не меняются.

### Template Method - генерация отчётов

Алгоритм записи в файл определён один раз в `AbstractReportGenerator`. Каждый из четырёх генераторов реализует только логику форматирования своих данных.

```java
public abstract class AbstractReportGenerator {
    protected void writeToFile(String path, String content) { ... }
    public abstract void generate(String outputPath);
}
```

---

## Иерархия классов

### Диаграмма 4 - Иерархия жителей и животных

![Диаграмма 4 - Иерархия жителей](docs/diagrams/diagram4_People_and_Animal_Class_Hierarchy.png)

### Диаграмма 5 - Иерархия устройств и паттерн State

![Диаграмма 5 - Иерархия устройств](docs/diagrams/diagram5_Device_Hierarchy_and_State_Pattern.png)

### Диаграмма 6 - Отчёты, AutoBuyer и обработчики событий

![Диаграмма 6 - Отчёты и AutoBuyer](docs/diagrams/diagram6_Reports_Template_Method_AutoBuyer_Chain_of_Responsibility.png)

---

## Жители и поведение

| Житель | Роль | maxEnergy | Специальное поведение |
|---|---|---|---|
| Otec | FATHER | 100 | Чинит устройства, едет за продуктами если нет ингредиентов |
| Matka | MOTHER | 90 | Готовит по расписанию, поливает цветы в солнечную погоду |
| Syn | SON | 120 | Приоритет - смотреть TV в своей комнате |
| Dcera | DAUGHTER | 110 | Стандартное поведение |
| Ded | GRANDFATHER | 60 | Включает термостат при холодной погоде |
| Kocka | CAT | - | Независимо бродит по дому, логирует WANDERED_TO / SLEEPING |

### Система прав (PermissionSet)

Каждый житель имеет набор правил `PermissionRule` - какие действия он может выполнять с какими типами устройств. Кот прав не имеет. Отец имеет полный доступ. Дети могут управлять только освещением, TV и аудио.

---

## Система устройств

В доме поддерживается 21 тип устройств (`DeviceType`):

```
SMART_LIGHT, GROUP_LIGHT, GARDEN_LIGHT, SMART_LOCK,
MOTION_SENSOR, DOOR_WINDOW_SENSOR, SMOKE_GAS_SENSOR,
WATER_LEAK_SENSOR, AIR_QUALITY_SENSOR, OUTDOOR_CAMERA,
THERMOSTAT, HUMIDIFIER_AC, SMART_WASHING_MACHINE,
MULTIROOM_AUDIO, SMART_TV, SMART_MIRROR, SMART_BLINDS,
IRRIGATION_SYSTEM, PET_FEEDER, SMART_COFFEE_MACHINE, FRIDGE
```

### Вероятности поломки

Каждый тик включённое устройство (`OnState`) случайно может сломаться. Вероятность зависит от последнего пользователя:

| Пользователь | Вероятность поломки за тик |
|---|---|
| Son | 0.50% (50/10000) |
| Daughter | 0.35% (35/10000) |
| Остальные | 0.20% (20/10000) |

При поломке публикуется событие `DEVICE_BROKEN` через `EventBus`. `FatherHandler` перехватывает его, Father в следующем тике идёт чинить.

### AutoBuyer - автоматические покупки

Жители имеют список желаний (`desires`). Если нужного устройства нет в доме - `AutoBuyer` покупает его и размещает в логичной комнате. Каждый тип имеет индивидуальный лимит:

| Лимит | Устройства |
|---|---|
| 1 (уникальные) | Fridge, AC, Washing Machine, Thermostat, Lock, Irrigation, Coffee Machine, Pet Feeder |
| 2 | TV, Audio, Mirror, Camera, все сенсоры |
| 4-6 | Лампочки, жалюзи |

---

## Система еды и шопинга

### Расписание приёмов пищи

| Приём | Время | Ингредиенты |
|---|---|---|
| Breakfast | 08:00-10:00 | Eggs x2, Oatmeal x1 |
| Lunch | 12:00-14:00 | Soup x2 |
| Dinner | 18:00-20:00 | Steak x2 |

### Начальный запас холодильника

| Продукт | Начало | Максимум |
|---|---|---|
| Eggs | 4 | 8 |
| Oatmeal | 2 | 6 |
| Soup | 2 | 4 |
| Steak | 1 | 4 |

Steak намеренно стартует с запасом `1` при норме `2` - это гарантирует поездку Father в магазин до ужина при каждом запуске.

### Логика шопинга Father

За 30 минут до каждого приёма пищи (`07:30`, `11:30`, `16:30`) Father проверяет холодильник. Если ингредиентов недостаточно:

1. `location = null` - исчезает с карты визуализатора
2. Лог: `SHOPPING: Left for groceries (4 ticks ~1h)`
3. Через 4 тика возвращается и вызывает `fridge.restockFull()`
4. Лог: `RESTOCK: Fridge fully restocked`

Проверка выполняется в `performStep()` до спортивной логики - шопинг имеет наивысший приоритет и прерывает любую текущую активность.

---

## Система погоды

Погода меняется каждые 8 тиков (~2 часа) случайно с весовыми вероятностями:

| Погода | Вероятность | Эффект |
|---|---|---|
| SUNNY | 50% | Mother поливает растения (включает IRRIGATION_SYSTEM) |
| CLOUDY | 30% | Нет эффекта |
| RAINY | 15% | Блокирует все активности в Garden |
| COLD | 5% | Grandfather включает термостат |

При плохой погоде (`RAINY` / `COLD`) методы `tryFindAndUseSport()` и `interactWithDevice()` исключают комнату Garden из списка кандидатов.

---

## Система энергии

Каждый житель имеет параметр `energy` (0 - maxEnergy). При низкой энергии житель идёт отдыхать в свою домашнюю комнату.

| Событие | Изменение энергии |
|---|---|
| Один тик занятий спортом | -15 |
| Один тик отдыха | +10 |
| Один тик без спорта | +3 |

| Роль | maxEnergy |
|---|---|
| GRANDFATHER | 60 |
| MOTHER | 90 |
| FATHER | 100 |
| DAUGHTER | 110 |
| SON | 120 |

Когда `energy < 30` - житель прерывает любую активность и идёт отдыхать. Когда `energy >= 70` - снова готов к спорту.

---

## Визуализатор

Десктопное Swing-приложение. Карта дома 3x3 в пиксельном стиле.

```
[Garage]       [Garden]      [  -  ]
[SonRoom]      [LivingRoom]  [Kitchen]
[DaughterRoom] [Bathroom]    [  -  ]
```

### Архитектура двух таймеров

Визуализатор работает на двух независимых `javax.swing.Timer`:

```
animTimer  (30ms)     - плавно двигает спрайты к целевым позициям (lerp-интерполяция)
                        перерисовывает экран 33 раза в секунду
                        не знает ничего о симуляции

stepTimer  (настр.)   - запускает один тик через engine.runStep()
                        обновляет целевые позиции спрайтов
                        частота регулируется слайдером скорости
```

### Функции GUI

| Функция | Описание |
|---|---|
| Пиксельные спрайты | Каждый житель - цветной спрайт с кодом роли (OT, MA, SY, DC, DE, KK) |
| Плавная анимация | Lerp-интерполяция положения спрайтов (30 fps) |
| Пузыри с действиями | Над спрайтом показывается последнее действие из ActivityLog |
| Полоска энергии | Зелёная - жёлтая - красная под каждым спрайтом |
| Усталость | Полупрозрачный спрайт + символ `z` когда житель отдыхает |
| Шопинг Father | Спрайт исчезает с карты, в углу надпись `Otec: SHOPPING` |
| Индикатор погоды | В статус-баре: `☀ Sunny / ☁ Cloudy / ☂ Rainy / ❄ Cold` |
| Счётчик устройств | В каждой комнате: `dev:вкл/всего` |
| Кликабельный холодильник | Клик на Kitchen - попап с текущим запасом продуктов |
| Кнопка назад | Snapshot-based rewind - шаг назад через сохранённые снимки состояния |

### Snapshot-based rewind

Перед каждым тиком `doStep()` сохраняет снимок (`Snapshot`) - позиции всех жителей и животных, последние действия, текст лога, время симуляции. При нажатии `◀ Prev` последний снимок восстанавливается. Это визуальный откат - состояние Java-объектов (устройства, инвентарь) не откатывается.

---

## Отчёты

Нажать кнопку `📋 Reports` после запуска симуляции. Файлы сохраняются в папку `output/`:

| Файл | Содержимое |
|---|---|
| `house_configuration_report.txt` | Полная структура дома - этажи, комнаты, устройства, жители |
| `activity_report.txt` | Лог всех действий всех жителей с временными метками |
| `event_report.txt` | Поломки устройств, кем обработаны, через какой Handler |
| `consumption_report.txt` | Потребление электричества (кВт·ч), воды (л), газа (м³) по устройствам |

---

## Запуск проекта

**Требования:** Java 23+, Maven 3.x

```bash
git clone https://github.com/ddbrdpl/smarthome_simulator.git
cd smarthome_simulator
mvn compile
mvn exec:java -Dexec.mainClass="cz.cvut.fel.omo.smarthome.Main"
```

### Управление в GUI

| Кнопка | Действие |
|---|---|
| `▶ Play` | Автозапуск симуляции |
| `Next ▶` | Один тик вперёд (15 мин игрового времени) |
| `◀ Prev` | Шаг назад (визуальный откат) |
| `↺ Reset` | Перезапуск |
| `📋 Reports` | Генерация отчётов в `output/` |
| Слайдер скорости | Управление скоростью воспроизведения |
| Клик на Kitchen | Просмотр содержимого холодильника |

---

## Конфигурация

Файл `src/main/resources/house.json` - полная конфигурация дома:

```json
{
  "rooms":   [ { "name": "Kitchen", "type": "KITCHEN" } ],
  "persons": [ { "id": "p1", "name": "Otec", "role": "FATHER", "room": "LivingRoom" } ],
  "devices": [ { "id": "d1", "name": "Kitchen Fridge", "type": "FRIDGE", "room": "Kitchen" } ],
  "sports":  [ { "id": "s1", "type": "TREADMILL", "room": "Garage" } ]
}
```

Для изменения состава дома достаточно отредактировать JSON - код менять не нужно.

---

## Структура проекта

```
src/main/java/cz/cvut/fel/omo/smarthome/
├── config/
│   ├── Configuration.java              - загрузка house.json через Jackson
│   ├── DeviceFactory.java              - фабрика устройств (Factory)
│   ├── PersonFactory.java              - фабрика жителей (Factory)
│   └── *Definition.java                - DTO для десериализации JSON
├── consumption/
│   ├── ConsumptionLog.java
│   ├── ConsumptionProfile.java         - профиль потребления устройства
│   └── ConsumptionRecord.java
├── devices/
│   ├── Device.java                     - абстрактный класс устройства
│   ├── DeviceState.java                - интерфейс состояния (State)
│   ├── OnState.java                    - включено (случайная поломка)
│   ├── OffState.java                   - выключено
│   ├── BrokenState.java                - сломано
│   ├── GenericDevice.java              - стандартное устройство
│   ├── Fridge.java                     - холодильник с системой продуктов
│   ├── DeviceType.java                 - enum 21 тип устройства
│   └── FoodType.java                   - enum EGGS, OATMEAL, SOUP, STEAK
├── events/
│   ├── EventBus.java                   - шина событий (Observer)
│   ├── EventListener.java              - интерфейс слушателя
│   ├── EventHandler.java               - интерфейс обработчика (CoR)
│   ├── AbstractEventHandler.java       - базовый обработчик цепочки
│   ├── FatherHandler.java              - обрабатывает DEVICE_BROKEN
│   ├── MotherHandler.java              - обрабатывает SMOKE_ALERT
│   ├── GrandfatherHandler.java         - обрабатывает TEMPERATURE_LOW
│   ├── FallbackHandler.java            - catch-all обработчик
│   ├── SystemEventListener.java        - запускает цепочку обработчиков
│   ├── Event.java
│   └── EventType.java
├── house/
│   ├── SmartHomeContext.java           - Singleton, центральный контекст
│   ├── Floor.java
│   └── Room.java
├── logs/
│   ├── ActivityLog.java
│   ├── ActivityEntry.java
│   ├── EventLog.java
│   └── EventEntry.java
├── people/
│   ├── visualization/
│   │   └── SimulationVisualizer.java   - Swing GUI визуализатор
│   ├── Animal.java                     - абстрактный класс животных
│   ├── Cat.java                        - кошка (блуждает по дому)
│   ├── Person.java                     - базовый класс жителя
│   ├── Father.java                     - чинит устройства, шопинг
│   ├── Mother.java                     - готовит, поливает цветы
│   ├── Son.java                        - приоритет TV
│   ├── Grandfather.java                - реагирует на холод
│   ├── PermissionSet.java              - набор прав жителя
│   ├── PermissionRule.java             - одно правило доступа
│   ├── Role.java                       - enum ролей
│   └── DeviceAction.java               - enum TURN_ON / TURN_OFF
├── reports/
│   ├── ReportGenerator.java            - интерфейс (Template Method)
│   ├── AbstractReportGenerator.java    - базовый генератор
│   ├── HouseConfigurationReportGenerator.java
│   ├── ActivityReportGenerator.java
│   ├── EventReportGenerator.java
│   └── ConsumptionReportGenerator.java
├── shop/
│   ├── AutoBuyer.java                  - покупает устройства с лимитами
│   └── ShopContext.java                - интерфейс (ISP из SOLID)
├── simulation/
│   ├── SimulationEngine.java           - главный цикл симуляции
│   ├── MealTime.java                   - расписание приёмов пищи
│   ├── Weather.java                    - enum погоды с весами
│   └── WeatherService.java             - сервис смены погоды
├── sports/
│   ├── SportEquipment.java             - спортивный инвентарь с таймером
│   └── SportType.java                  - enum типов инвентаря
└── Main.java                           - точка входа + генерация отчётов

src/main/resources/
└── house.json                          - конфигурация дома

output/
├── house_configuration_report.txt
├── activity_report.txt
├── event_report.txt
└── consumption_report.txt
```

---

## Планы на v2

- Расписание дня - утром все на кухне, вечером в гостиной
- Взаимодействие между жителями - конкуренция за тренажёр и TV
- Ночной режим - после 22:00 свет гасится, все расходятся по комнатам
- Unit тесты - покрытие `Fridge`, `MealTime`, `AutoBuyer`, `PermissionSet`
- Более разнообразное меню с расширенным списком продуктов
