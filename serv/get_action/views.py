import json, random
from django.http import HttpResponse, JsonResponse
from django.core import serializers
from django.db.models import F, Func, Value, FloatField

from saver.models import Action, State, CreepState


def get_action(request):
    print("entered")
    json_state = json.loads(request.body.decode("utf-8"), )

    s = State()

    deserialized = json_state["game_info"]

    s.our_team = deserialized[0]
    s.enemy_hero = deserialized[1][14:]

    deserialized = json_state["self_info"]

    s.damage = deserialized[0]
    s.lvl = deserialized[1]
    s.gold = deserialized[2]

    s.hp = deserialized[3]
    s.max_hp = deserialized[4]
    s.mana = deserialized[5]
    s.max_mana = deserialized[6]

    s.facing = deserialized[7]

    s.available1 = deserialized[8]
    s.available2 = deserialized[9]
    s.available3 = deserialized[10]
    s.available4 = deserialized[11]

    s.x = deserialized[12]
    s.y = deserialized[13]

    deserialized = json_state["damage_info"]

    s.time_since_damage_by_hero = deserialized[0]
    s.time_since_damage_by_creep = deserialized[1]
    s.time_since_damage_by_tower = deserialized[2]

    deserialized = json_state["enemy_info"]

    s.enemy_visible = deserialized[0]

    s.enemy_lvl = deserialized[1]
    s.enemy_damage = deserialized[2]

    s.enemy_hp = deserialized[3]
    s.enemy_max_hp = deserialized[4]
    s.enemy_mana = deserialized[5]
    s.enemy_max_mana = deserialized[6]

    s.enemy_facing = deserialized[7]
    s.enemy_x = deserialized[8]
    s.enemy_y = deserialized[9]

    deserialized = json_state["tower_info"]

    s.tower_hp = deserialized[1]
    s.enemy_tower_hp = deserialized[0]

    ###

    deserialized = json_state["ally_creeps_info"]

    n1 = len(deserialized)
    ocs = []
    for i, creep in zip(range(n1), deserialized):
        c = CreepState()
        # c.type = creep["type"]
        c.num = i + 1
        c.isEnemy = False

        c.hp = creep[0]
        c.max_hp = creep[1]
        c.x = creep[2]
        c.y = creep[3]
        c.state_host = s.id

        ocs.append(c)
    '''
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

        ecs.append(c)'''

    print("got_data")

    restr = 500

    list = State.objects.filter(our_team__exact=s.our_team).filter(x__gt = s.x - restr)
    list = list.filter(x__lt = s.x + restr)
    list = list.filter(y__gt = s.y - restr)
    list = list.filter(y__lt = s.y + restr).values()

    f1 = F('x') - Value(s.x, output_field=FloatField())
    f2 = f1 ** 2
    g1 = F('y') - Value(s.y, output_field=FloatField())
    g2 = g1 ** 2

    t1 = F('hp') - Value(s.hp, output_field=FloatField())
    t2 = F('mana') - Value(s.mana, output_field=FloatField())
    t3 = F('lvl') - Value(s.lvl, output_field=FloatField())
    h1 = 150 * (Func(t1, function='ABS') + Func(t2, function='ABS')) + 200 * Func(t3, function='ABS')

    t1 = Func(F('tower_hp') - Value(s.tower_hp, output_field=FloatField()), function='ABS')
    t2 = Func(F('enemy_tower_hp') - Value(s.enemy_tower_hp, output_field=FloatField()), function='ABS')
    h2 = t1 + t2

    dist = f2 + g2 + h1 + h2


    list = list.annotate(dist=dist).order_by(dist)[:15]
    closests = [Action.objects.get(pk=s['action_done_id']) for s in list]


    print(len(closests))
    act = find_nice_action(closests)

    print([act.action_type, act.param, act.dx, act.dy])
    return HttpResponse([act.action_type, ',', act.param, ',', act.dx, ',', act.dy])

def find_nice_action(closests):
    buckets = [[], [], [], [], [], [], []]

    for act in closests:
        buckets[act.action_type + 1].append(act)

    if len(buckets[2]) + len(buckets[3]) + len(buckets[4]) > 7:
        buckets = buckets[2:5]

    buckets = buckets[1:-1]

    buckets = sorted(buckets, key=lambda l: -len(l))

    if len(buckets[0]) == 0:
        return random.choice(closests)

    return random.choice(buckets[0])
