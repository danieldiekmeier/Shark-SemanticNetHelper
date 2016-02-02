# SemanticNetHelper

Helper class to use with SemanticNet objects of the [Shark Framework](http://sharksystem.net/). You can pass the SemanticNet instance to the helper class to perform different checks or transform the SemanticNet to follow certain rules.

## Usage

SemanticNetHelper features five methods. Three of them check the given SemanticNet for a certain property of SemanticNets, the other two change the SemanticNet so it has two of these properties.

### `boolean isTaxonomy(SemanticNet semanticNet, String predicate)`

Checks if the SemanticNet instance is a correct taxonomy using Tarjan's Strongly Connected Components Algorithm: https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm

@param `semanticNet` A SemanticNet, probably with some SemanticTags that have some kind of connection

@param `predicate` The predicate that should be checked.

@return `true` if the connections of Predicate in the SemanticNet form a taxonomy, `false` if there are circular dependencies between the tags


### `boolean isTransitive(SemanticNet semanticNet, String predicate)`

Checks if all the connections of predicate in the SemanticNet instance form a transitive net.

@param `semanticNet` A SemanticNet, probably with some SemanticTags that have some kind of connection

@param `predicate` The predicate that should be checked.

@return `true` if all connections of the Predicate are transitive, `false` if they are not


### `boolean isSymmetric(SemanticNet semanticNet, String predicate)`

@param `semanticNet` A SemanticNet, probably with some SemanticTags that have some kind of connection

@param `predicate` The predicate that should be checked.

@return `true` if all connections with the predicate are symmetric


### `void makeSymmetric(SemanticNet semanticNet, String predicate)`

Changes the SemanticNet to make all the connections of predicate symmetric, that means that every connection that previously only went from A -> B, now also goes from B -> A.

(The SemanticNet is then guaranteed to pass .isSymmetric for the same predicate.)

@param `semanticNet` A SemanticNet, probably with some SemanticTags that have some kind of connection

@param `predicate` The predicate that should be checked.


### `void makeTransitive(SemanticNet semanticNet, String predicate)`

Changes the SemanticNet to make all the connections of predicate transitive. (The SemanticNet is then guaranteed to pass .isTransitive for the same predicate.)

@param `semanticNet` A SemanticNet, probably with some SemanticTags that have some kind of connection

@param `predicate` The predicate that should be checked.
