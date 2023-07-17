package tokenico.controllers.builders;

abstract class Fluent<B extends Fluent<B>> {
    protected abstract B thisObject();
}
