## Мебельная фабрика

Инструкция по запуску:

1. Склонировать репозиторий:

        $ git clone git://github.com/nastich/furniture-factory.git furniture-factory

2. Перейти в склонированный репозиторий:

        $ cd furniture-factory

3. Запустить SBT:

        $ sbt

4. Скомпилировать код и прогнать тесты:

        > test

5. Запустить сервер:

        > re-start

6. В браузере перейти по ссылке [заказа стула](http://localhost:8080/order?item=chair) или
   [заказа стола](http://localhost:8080/order?item=table)

7. Остановить приложение:

        > re-stop