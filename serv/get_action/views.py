import json
from django.http import HttpResponse, JsonResponse

from saver.models import Action, State, CreepState


def get_action(request):
    print("entered")

    json_data_state = request.GET.get('state')
    deserialized = json.loads(json_data_state)

    s = State()
    s.our_team = deserialized["ourTeam"]
    s.enemy_hero = deserialized["enemyName"]

    our_score = deserialized["ourScore"]
    enemy_score = deserialized["enemyScore"]
    s.score = our_score * 2 + enemy_score

    s.x = deserialized["ourX"]
    s.y = deserialized["ourY"]
    s.facing = deserialized["ourFacing"]

    s.lvl = deserialized["ourLvl"]
    s.damage = deserialized["ourAttackDamage"]
    s.gold = deserialized["ourGold"]

    s.hp = deserialized["ourHp"]
    s.max_hp = deserialized["ourMaxHp"]
    s.mana = deserialized["ourMana"]
    s.max_mana = deserialized["ourMaxMana"]

    s.available1 = deserialized["isOurAbility1Available"]
    s.available2 = deserialized["isOurAbility2Available"]
    s.available3 = deserialized["isOurAbility3Available"]
    s.available4 = deserialized["isOurAbility4Available"]

    s.time_since_damage_by_hero = deserialized["timeSinceDamagedByHero"]
    s.time_since_damage_by_creep = deserialized["timeSinceDamagedByCreep"]
    s.time_since_damage_by_tower = deserialized["timeSinceDamagedByTower"]

    s.enemy_visible = deserialized["isEnemyVisible"]
    s.enemy_x = deserialized["enemyX"]
    s.enemy_y = deserialized["enemyY"]
    s.enemy_facing = deserialized["enemyFacing"]

    s.enemy_lvl = deserialized["enemyLvl"]
    s.enemy_damage = deserialized["enemyAttackDamage"]

    s.enemy_hp = deserialized["enemyHp"]
    s.enemy_max_hp = deserialized["enemyMaxHp"]
    s.enemy_mana = deserialized["enemyMana"]
    s.enemy_max_mana = deserialized["enemyMaxMana"]

    s.tower_hp = deserialized["ourTowerHp"]
    s.enemy_tower_hp = deserialized["enemyTowerHp"]

    ###

    n1 = len(deserialized["ourCreeps"])
    ocs = []
    for i, creep in zip(range(n1), deserialized["ourCreeps"]):
        c = CreepState()
        c.type = creep["type"]
        c.num = i + 1
        c.isEnemy = False

        c.hp = creep["hp"]
        c.max_hp = creep["maxHp"]
        c.x = creep["x"]
        c.y = creep["y"]
        c.state_host = s.id

        ocs.append(c)

    n2 = len(deserialized["enemyCreeps"])
    ecs = []
    for i, creep in zip(range(n2), deserialized["enemyCreeps"]):
        c = CreepState()
        c.type = creep["type"]
        c.num = i + 1
        c.isEnemy = True

        c.hp = creep["hp"]
        c.max_hp = creep["maxHp"]
        c.x = creep["x"]
        c.y = creep["y"]
        c.state_host_id = s.id

        ecs.append(c)

    list = State.objects.all()

    min = 1e10
    to_ret = None
    for sl in list:
        d = dist(s, sl)
        if d < min:
            to_ret = sl.action_done

    return JsonResponse(to_ret, False)


def dist(s1, s2):
    d1 = (s1.x - s2.x) ** 2 + (s1.y - s2.y) ** 2
    d2 = 50 * (abs(s1.hp - s2.hp) + abs(s1.mana - s2.mana) + abs(s1.lvl - s2.lvl))

    d3 = 0
    if s1.enemy_visible ^ s2.enemy_visible:
        d3 = 100000
    else:
        d3 = (s1.enemy_x - s2.enemy_x) ** 2 + (s1.enemy_y - s2.enemy_y) ** 2

    return d1 + d2 + d3
