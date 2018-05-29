Action = {}

local bot = GetBot()

local ACTION_MOVE = "0"
local ACTION_ATTACK_HERO = "1"
local ACTION_ATTACK_CREEP = "2"
local ACTION_USE_ABILITY = "3"
local ACTION_ATTACK_TOWER = "4"
local ACTION_ATTACK_OUR_CREEP = "5"
-- DOTO: think about discrete moving
local ACTION_MOVE_DISCRETE = "6"
local ACTION_DO_NOTHING = "-1"

local failed = 0
local active = 0
local cooldown = 20

local ABILITY = {
    bot:GetAbilityByName('nevermore_shadowraze1'),
    bot:GetAbilityByName('nevermore_shadowraze2'),
    bot:GetAbilityByName('nevermore_shadowraze3'),
    bot:GetAbilityByName('nevermore_requiem')
}


--- Move by delta vector.
-- @param parameters of action; 3 and 4 are for dx and dy
--
function move_delta(param)
    local position = bot:GetLocation()

    print('MOVE', param[3], param[4])
    position[1] = param[3]
    position[2] = param[4]

    bot:Action_MoveDirectly(position)
end

-- 16 possible directions: 0-15
function move_discrete(direction)
    print('MOVE DISCRETE', direction[2])
    local position = bot:GetLocation()
    local x = 100
    local y = 0
    local theta = 0 + direction[2] * (math.pi / 8)
    local sin_theta = math.sin(theta)
    local cos_theta = math.cos(theta)

    position[1] = position[1] + x * cos_theta - y * sin_theta
    position[2] = position[2] + x * sin_theta + y * cos_theta
    bot:Action_MoveDirectly(position)
end

--- Attack enemy hero.
--
function attack_hero()
    print('ATTACK HERO')
    local enemy_table = GetUnitList(UNIT_LIST_ENEMY_HEROES)
    local enemy
    failed = 1
    if #enemy_table > 0 then
        enemy = enemy_table[1]
        if GetUnitToUnitDistance(bot, enemy) < 1500 then
            failed = 0
            bot:Action_AttackUnit(enemy, false)
            active = cooldown
        end
    end
end

--- Use ability.
-- @param ability_idx index of ability in 'ABILITY' table.
--
function use_ability(ability_idx)
    print('USE ABILITY', ability_idx)
    local ab = ABILITY[ability_idx]
    if ab:IsFullyCastable() then
        bot:Action_UseAbility(ab)
        active = cooldown
    else
        failed = 1
    end
end

--- Attack enemy creep.
-- @param creep_idx index of creep in nearby creeps table.
--
function attack_creep(creep_idx)
    failed = 1
    print('ATTACK CREEP', creep_idx)
    local enemy_creeps = bot:GetNearbyCreeps(1500, true)
    local enemy
    if #enemy_creeps >= 1 then
        enemy = enemy_creeps[1]
        if GetUnitToUnitDistance(bot, enemy) < 1500 then
            failed = 0
            bot:Action_AttackUnit(enemy, false)
            active = cooldown
        end
    end
end

function attack_tower()
    print('ATTACK TOWER')
    local towers = bot:GetNearbyTowers(1500, true)
    if #towers > 0 then
        bot:Action_AttackUnit(towers[1], false)
        active = cooldown
    else
        failed = 1
    end
end

function upgrade_abilities()
    -- TODO: invent a stategy
    bot:ActionImmediate_LevelAbility('nevermore_shadowraze1')
    bot:ActionImmediate_LevelAbility('nevermore_requiem')
end

-- TODO: write function to attack friendly creeps


--- Execute given action.
-- @param action_info action info {'action': action id, 'params': action parameters}
--
function Action.execute_action(action_info)
    local action = action_info[1]
    failed = 0

    if active > 0 then
        active = active - 1
        return failed
    end

    upgrade_abilities()

    if action == ACTION_MOVE then
        move_delta(action_info)
    elseif action == ACTION_ATTACK_HERO then
        attack_hero()
    elseif action == ACTION_USE_ABILITY then
        use_ability(action_info[2])
    elseif action == ACTION_ATTACK_CREEP then
        attack_creep(action_info[2])
    elseif action == ACTION_ATTACK_TOWER then
        attack_tower()
    elseif action == ACTION_MOVE_DISCRETE then
        move_discrete(action_info)
    elseif action == ACTION_DO_NOTHING then
        -- do nothing
    end

    return failed
end

return Action;
