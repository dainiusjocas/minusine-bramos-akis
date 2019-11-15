.PHONY: test
test:
	clojure -A:test

uberjar:
	rm -rf classes
	mkdir classes
	./compile.clj
	clojure -A:uberjar --target target/minusine-bramos-akis.jar

package-mba: uberjar
	aws cloudformation package \
        --template-file stack.yml \
        --s3-bucket mba-labs \
        --s3-prefix mba \
        --output-template-file /tmp/mba-stack.yml

stack-name=minusine-bramos-akis-dev

deploy-mba: package-mba
	aws cloudformation deploy \
        --template-file /tmp/mba-stack.yml \
        --stack-name $(stack-name) \
        --capabilities CAPABILITY_IAM \
        --no-fail-on-empty-changeset

setup-ui:
	yarn add shadow-cljs

release-ui: setup-ui
	./node_modules/shadow-cljs/cli/runner.js -A:cljs release frontend

dev-ui: setup-ui
	./node_modules/shadow-cljs/cli/runner.js -A:cljs watch frontend
