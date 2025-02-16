package core.basesyntax.service.performer;

import static core.basesyntax.model.Operation.BALANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import core.basesyntax.dao.FruitStorageDao;
import core.basesyntax.dao.FruitStorageDaoImpl;
import core.basesyntax.db.FruitStorage;
import core.basesyntax.model.FruitTransaction;
import core.basesyntax.model.Operation;
import core.basesyntax.service.parser.Parser;
import core.basesyntax.strategy.OperationHandler;
import core.basesyntax.strategy.OperationStrategy;
import core.basesyntax.strategy.impl.BalanceOperationHandler;
import core.basesyntax.strategy.impl.OperationStrategyImpl;
import core.basesyntax.strategy.impl.PurchaseOperationHandler;
import core.basesyntax.strategy.impl.ReturnOperationHandler;
import core.basesyntax.strategy.impl.SupplyOperationHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionPerformerImplTest {
    private static OperationStrategy operationStrategy;
    private static FruitStorageDao fruitStorageDao;
    private static TransactionPerformer transactionPerformer;
    private static List<FruitTransaction> transactions;
    private static Parser parser;

    @BeforeAll
    static void beforeAll() {
        parser = new Parser();
        fruitStorageDao = new FruitStorageDaoImpl();
        Map<Operation, OperationHandler> operationHandlerMap = new HashMap<>();
        operationHandlerMap.put(BALANCE, new BalanceOperationHandler(fruitStorageDao));
        operationHandlerMap.put(Operation.PURCHASE, new PurchaseOperationHandler(fruitStorageDao));
        operationHandlerMap.put(Operation.RETURN, new ReturnOperationHandler(fruitStorageDao));
        operationHandlerMap.put(Operation.SUPPLY, new SupplyOperationHandler(fruitStorageDao));

        operationStrategy = new OperationStrategyImpl(operationHandlerMap);
        transactionPerformer = new TransactionPerformerImpl(operationStrategy);
    }

    @BeforeEach
    void setUp() {
        FruitStorage.fruitToStorageQuantityMap.clear();
    }

    @Test
    void performTransactions_withValidData_ok() {
        transactions = parser.parseListToTransactionList(List.of(
                "fruit,quantity",
                "b,apple,120",
                "s,banana,23",
                "r,banana,37",
                "p,apple,100"
        ));

        transactionPerformer.performTransactions(transactions);
        int bananaActualQuantity = fruitStorageDao.getQuantity("banana");
        int appleActualQuantity = fruitStorageDao.getQuantity("apple");

        int appleExpectedQuantity = 20;
        int bananaExpectedQuantity = 60;
        assertEquals(appleExpectedQuantity, appleActualQuantity);
        assertEquals(bananaExpectedQuantity, bananaActualQuantity);
    }

    @Test
    void performTransactions_withEmptyData_notOk() {
        transactions = Collections.emptyList();
        transactionPerformer.performTransactions(transactions);
        assertEquals(0, FruitStorage.fruitToStorageQuantityMap.size());
    }
}
