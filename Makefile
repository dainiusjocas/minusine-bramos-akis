.PHONY: test
test:
	clojure -A:test

uberjar:
	rm -rf classes
	mkdir classes
	clojure -R:uberjar compiler.clj
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
