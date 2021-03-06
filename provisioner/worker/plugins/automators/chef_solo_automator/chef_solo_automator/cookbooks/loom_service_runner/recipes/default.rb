#
# Cookbook Name:: loom_service_runner
# Recipe:: default
#
# Copyright (C) 2013-2014 Continuuity, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# We run through all of the services listed, and start/stop them, if a service resource exists

if node['loom']['node'].has_key?('services')
  node['loom']['node']['services'].each do |k, v|
    if resources(service: k)
      log "service-#{v}-#{k}" do
        message "Service: #{k}, action: #{v}"
      end
      ruby_block "service-#{v}-#{k}" do
        block do
          resources("service[#{k}]").run_action(v.to_sym)
        end # block
      end # ruby_block
    end # if
  end # each
end # if
