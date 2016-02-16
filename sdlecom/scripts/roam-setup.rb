#!/usr/bin/env ruby

require 'fileutils'

def reset_source(project_path)
  Dir.chdir(project_path) do
    `git checkout -- **`
    `git clean -fdX`
    `git clean -fX`
    `git clean -fd`
    `git clean -f`
  end
end

def print_usage()
  puts("Usage: #{File.basename($0)} countyName ecomEnv (local or qa) reset (optional)")
end

project_path = "#{File.dirname($0)}/../.."
files_path = "#{ENV["HOME"]}/files"

if ARGV.length == 0 || (ARGV.length == 1 && ARGV[0] == "help")
  print_usage()
  exit
end

if ARGV.length == 1 && ARGV[0] == "resetonly"
  reset_source(project_path)
  exit
end

county = ARGV[0]
environment = ARGV[1]

if ARGV[2] && ARGV[2] == "reset"
  reset_source(project_path)
end

temp_build_props_path = "#{project_path}/sdlecom/build.properties.temp"
final_build_props_path = "#{project_path}/sdlecom/build.properties"
source_build_props_path = "#{project_path}/sdlecom/build-props/#{county}-test.build.properties"

File.open(temp_build_props_path, 'w') do |fo|
  fo.puts('client.templateroot=../..')
  File.foreach(source_build_props_path) do |li|
    li = li.gsub(/^ecommerce\.clienturl=.*$/, "ecommerce.clienturl=http://localhost:4001/sdlecom/")
    li = li.gsub(/^warfilename=.*$/, "warfilename=sdlecom.war")
    li = li.gsub(/^prefixPathForLogs=.*$/, "prefixPathForLogs=#{files_path}/logs/roam/#{county}")
    li = li.gsub(/^ecommerce.isPSOOnlyMachine=true$/, "ecommerce.isPSOOnlyMachine=false")
    li = li.gsub(/^threeTier=threeTier$/, "")
    li = li.gsub(/^indexServerUrl=http:\/\/nv-rd1.amcad.com\/azroamindex\/$/, "")
    if environment == "local"
      li = li.gsub(/^ecommerce\.serverurl=.*$/, "ecommerce.serverurl=http://localhost:2001/ecom/")
      li = li.gsub(/^ecom\.facadeservice\.wsdl=.*$/, "ecom.facadeservice.wsdl=http://localhost:2001/ecom/service/EComFacadeService?wsdl")
      li = li.gsub(/^ecom\.facadeservice\.address=.*$/, "ecom.facadeservice.address=http://localhost:2001/ecom/service/EComFacadeService")
      li = li.gsub(/^ecom.facadeservice.resturl=.*$/, "ecom.facadeservice.resturl=http://localhost:2001/ecom/service/EComFacadeServiceRS/")
    elsif environment == "qa"
      li = li.gsub(/^ecommerce\.serverurl=.*$/, "ecommerce.serverurl=https://eaccept.granicuslabs.com/ecom/")
      li = li.gsub(/^ecom\.facadeservice\.wsdl=.*$/, "ecom.facadeservice.wsdl=https://eaccept.granicuslabs.com/ecom/service/EComFacadeService?wsdl")
      li = li.gsub(/^ecom\.facadeservice\.address=.*$/, "ecom.facadeservice.address=https://eaccept.granicuslabs.com/ecom/service/EComFacadeService")
      li = li.gsub(/^ecom.facadeservice.resturl=.*$/, "ecom.facadeservice.resturl=https://eaccept.granicuslabs.com/ecom/service/EComFacadeServiceRS/")
    end
    fo.puts(li)
  end
end

FileUtils.cp(temp_build_props_path, final_build_props_path)
FileUtils.rm(temp_build_props_path)

File.readlines(final_build_props_path).each do |line|
  if line.start_with?("client.templatetype")
    if line.split("=")[1] == "SDLECOM"
      secure = true
    elsif line.split("=")[1] == "SDL"
      secure = false
    end
  end
end

Dir.chdir(project_path) do
  if secure
    `ant -f sdlecom/build.xml buildJenkins`
  else
    `ant -f sdlecom/buildNonSecurity.xml buildJenkins`
  end
end

source_system_props_path = "#{project_path}/sdlecom/WebContent/WEB-INF/classes/system.properties"
target_system_props_path = "#{project_path}/sdlecom/resources/system.properties"

FileUtils.cp(source_system_props_path, target_system_props_path)

temp_logback_xml_path = "#{project_path}/sdlecom/resources/logback.temp.xml"
logback_xml_path = "#{project_path}/sdlecom/resources/logback.xml"

File.open(temp_logback_xml_path, 'w') do |fo|
  File.foreach(logback_xml_path) do |li|
    li = li.gsub(/<!-- Commented for Prod <appender-ref ref="STDOUT"\/>  -->/, "<appender-ref ref=\"STDOUT\"\/>")
    fo.puts(li) unless li.match(/<appender-ref ref="EMAIL"\/>/)
  end
end

FileUtils.cp(temp_logback_xml_path, logback_xml_path)
FileUtils.rm(temp_logback_xml_path)

temp_xsearch_config_path = "#{project_path}/sdlecom/WebContent/WEB-INF/data/xsearch-config.temp.xml"
xsearch_config_path = "#{project_path}/sdlecom/WebContent/WEB-INF/data/xsearch-config.xml"

File.open(temp_xsearch_config_path, 'w') do |fo|
  File.foreach(xsearch_config_path) do |li|
    li = li.gsub(/(<index-root-directory>)<\!\[CDATA\[.*\]\]>(<\/index-root-directory>)/, 
        "\\1<![CDATA[#{files_path}/indexes/#{county}]]>\\2")
    fo.puts(li)
  end
end

FileUtils.cp(temp_xsearch_config_path, xsearch_config_path)
FileUtils.rm(temp_xsearch_config_path)
