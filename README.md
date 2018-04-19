# dota-things

The plan:
1) "scrapper.py - парсим dotabuff.com. Матчей с параметром skill_bracket=very_high_skill достаточно много и меньше мусора, по таймингу в 15 минут записываем в текстовый лог match_id. Если снять требования skill_bracket, то тайминг можно снизить до 3-5 минут и увеличить число итераций.  Пример работы в файле match_id.txt" - цитата и scrapper.py принадлежат Ярославу. Еще там в ссылке указано, что мы ищем только матчи 1x1 с героем Shadow Fiend, но при желании это можно менять (скорее всего, придется увеличить время работы, т.к. матчей с другими героями значительно меньше).

2) С помощью valvePython/dota2 (https://github.com/ValvePython/dota2) и ранее сгенерированных id формируем ссылки на скачивание реплеев, заодно проверяем реплеи на то, что они нам подходят (нужно, Shadow Fiend там выиграл). Сохраняем ссылки в файл match_links.txt.

3) Используя clarity, парсим скачанные реплеи. Сохраняем данные в базу данных.
