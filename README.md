# dota-things

The plan:
1) "scrapper.py - парсим dotabuff.com. Матчей с параметром skill_bracket=very_high_skill достаточно много и меньше мусора, по таймингу в 15 минут записываем в текстовый лог match_id. Если снять требования skill_bracket, то тайминг можно снизить до 3-5 минут и увеличить число итераций.  Пример работы в файле match_id.txt" - цитата и scrapper.py принадлежат Ярославу. match.id генерируемый, поэтому его здесь нет.

2) С помощью valvePython/dota2 (https://github.com/ValvePython/dota2) и щепотки магии формируем ссылку на скачивание реплеев. 

3) ???
