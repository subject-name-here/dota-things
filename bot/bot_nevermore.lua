local Observation = require(GetScriptDirectory() .. '/agent_utils/observation')
local Action = require(GetScriptDirectory() .. '/agent_utils/action')

local current_action

-- Bot to server comunication FSM.
local WHAT_NEXT = 0
local ACTION_RECEIVED = 1
local SEND_OBSERVATION = 2
local DO_NOTHING = 3
local fsm_state = SEND_OBSERVATION

local failed_action = 0
local this_bot = GetBot()

--- Executes received action.
-- @param action_info bot action
--
function execute_action(action_info)
    print("Execute order.", action_info)
    failed_action = Action.execute_action(action_info)
    if failed_action == 1 then
        print("Failed to execute action.", action_info)
    end
end


function string:split(sep)
   local sep, fields = sep or ":", {}
   local pattern = string.format("([^%s]+)", sep)
   self:gsub(pattern, function(c) fields[#fields+1] = c end)
   return fields
end

--- Send JSON message to bot server.
-- @param json_message message to send
-- @param route route ('/what_next' or '/observation')
-- @param callback on responce received callback
--
function send_message(json_message, route, callback)
    local req = CreateHTTPRequest(':22229' .. '/get/get_action')
    req:SetHTTPRequestRawPostBody('application/json', json_message)
    req:Send(function(result)     
        for k, v in pairs(result) do
            if k == 'Body' then
                if v ~= '' then
                    print("received from serv:")
                    print(v)
                    local r = v:split(',')
                    current_action = r
                    fsm_state = ACTION_RECEIVED
                else
		    -- For some reason we failed. Well, let's try again...
                    fsm_state = SEND_OBSERVATION
                end
            end
        end
    end)
end


--- Send JSON with current state info.
--
function send_observation_message()
    local _end = false

    if GetGameState() ~= 4 and GetGameState() ~= 5 then
        _end = true
        print('Bot: the game has ended.')
    end

    local msg = table_to_json(Observation.get_observation())

    send_message(msg, '/observation')
end


local last_time_sent = GameTime()

function Think()
    if fsm_state == SEND_OBSERVATION then
        print('Sending')
        fsm_state = DO_NOTHING
        send_observation_message()
        last_time_sent = GameTime()
    elseif fsm_state == ACTION_RECEIVED then
        fsm_state = SEND_OBSERVATION
        execute_action(current_action)
    elseif fsm_state == DO_NOTHING then
        --print("Do nothing since", last_time_sent)
    end
end
