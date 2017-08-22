.SILENT:
version = 0.0.1
name = "chickling/kmanager"
container_name = "chickling-kmanager"
cid=$(shell docker ps -aqf name=$(container_name))
help:
	echo "Commands: (kmanager version: $(version))"
	echo "	clean - remove java target folder"
	echo "	package - build maven package"
	echo "	run-java - run service with native java"
	echo "	run - run service with docker"
	echo "	build - build docker image"
	echo "	push - push docker image to docker hub"
	echo "	version - show version"
clean:
	rm -rf target
package: clean
	mvn package -X
run-java: package
	java -jar target/ChicklingKmanager.jar
run: remove_container
	docker run -it -p 8099:8099 --name $(container_name) $(name):$(version)
build: package
	cp target/ChicklingKmanager.jar docker/ && docker build -t $(name):$(version) ./docker && rm -f docker/ChicklingKmanager.jar
push: build
	docker push $(name):$(version)
test:
	echo "Tests not yet implemented"
remove_container:
    ifneq ($(cid), )
		echo "removing $(container_name)"
		docker rm $(container_name)
    endif
version: 
	echo $(version)