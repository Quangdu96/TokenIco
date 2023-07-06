// SPDX-License-Identifier: MIT
pragma solidity ^0.8.12;

import "@openzeppelin/contracts/security/ReentrancyGuard.sol";

import "./MyToken.sol";

/**
 * @title IcoManager
 * @dev 
 */
contract IcoManager is ReentrancyGuard {
    struct Sale {
        bool created;
        uint256 startTime;
        uint256 endTime;
        uint256 price; // of 1 MTK in wei
        bool openStatus;
        mapping(address => uint256) buyerToAllowance;
        uint256 totalSold;
    }

    struct BuyerAndAllowance {
        address buyer;
        uint256 allowance;
    }

    address         public immutable    myToken;
    address         public immutable    admin;
    address payable public immutable    incomeReceiver;
    bool            public              stopped;

    string[] public saleIds;

    mapping(string => Sale) public sales;

    event Sold(
        string  indexed saleId,
        address indexed buyer,
        uint256         amount);

    constructor(address incomeReceiver_) {
        myToken = address(new MyToken(msg.sender));
        admin = msg.sender;
        incomeReceiver = payable(incomeReceiver_);

        sales["PUBLIC_SALE"].created = true;
        saleIds.push("PUBLIC_SALE");
    }

    modifier onlyAdmin(string memory functionName) {
        require(msg.sender == admin,
            string.concat("IcoManager.", functionName, ": Caller is not admin"));
        _;
    }

    modifier existingSale(string calldata saleId, string memory functionName) {
        require(sales[saleId].created,
            string.concat("IcoManager.", functionName, ": Sale does not exist"));
        _;
    }

    /* ********************************************************************************************** */

    function addSale(
        string calldata saleId,
        uint256 startTime,
        uint256 endTime,
        uint256 price,
        bool openStatus
    ) external onlyAdmin("addSale") {
        require(!sales[saleId].created, "IcoManager.addSale: Sale already exists");

        saleIds.push(saleId);
        sales[saleId].created = true;
        sales[saleId].startTime = startTime;
        sales[saleId].endTime = endTime;
        sales[saleId].price = price;
        sales[saleId].openStatus = openStatus;
    }

    function editSaleStartTime(string calldata saleId, uint256 newStartTime)
    external onlyAdmin("editSaleStartTime") existingSale(saleId, "editSaleStartTime") {
        sales[saleId].startTime = newStartTime;
    }

    function editSaleEndTime(string calldata saleId, uint256 newEndTime)
    external onlyAdmin("editSaleEndTime") existingSale(saleId, "editSaleEndTime") {
        sales[saleId].endTime = newEndTime;
    }

    function editSalePrice(string calldata saleId, uint256 newPrice)
    external onlyAdmin("editSalePrice") existingSale(saleId, "editSalePrice") {
        sales[saleId].price = newPrice;
    }

    function editSaleOpenStatus(string calldata saleId, bool newOpenStatus)
    external onlyAdmin("editSaleOpenStatus") existingSale(saleId, "editSaleOpenStatus") {
        sales[saleId].openStatus = newOpenStatus;
    }

    /* ********************************************************************************************** */

    // Exist only because web3j generated wrapper class doesn't support nested mapping
    function getAllowance(string calldata saleId, address buyer) public view returns(uint256) {
        return sales[saleId].buyerToAllowance[buyer];
    }

    // Batch set/increase allowance
    function batchSetAllowance(string calldata saleId, BuyerAndAllowance[] calldata buyerAndAllowanceList)
    external onlyAdmin("batchSetAllowance") existingSale(saleId, "batchSetAllowance") {
        mapping(address => uint256) storage buyerToAllowance = sales[saleId].buyerToAllowance;
        for (uint256 i = 0; i < buyerAndAllowanceList.length; i++) {
            buyerToAllowance[buyerAndAllowanceList[i].buyer] = buyerAndAllowanceList[i].allowance;
        }
    }

    function batchIncreaseAllowance(string calldata saleId, BuyerAndAllowance[] calldata buyerAndAllowanceList)
    external onlyAdmin("batchIncreaseAllowance") existingSale(saleId, "batchIncreaseAllowance") {
        mapping(address => uint256) storage buyerToAllowance = sales[saleId].buyerToAllowance;
        for (uint256 i = 0; i < buyerAndAllowanceList.length; i++) {
            buyerToAllowance[buyerAndAllowanceList[i].buyer] += buyerAndAllowanceList[i].allowance;
        }
    }

    /* ********************************************************************************************** */

    // Buy
    function buy(string calldata saleId, uint256 amount)
    external payable nonReentrant existingSale(saleId, "buy") {
        require(!stopped, "IcoManager.buy: Contract has been stopped");

        Sale storage sale = sales[saleId];

        require(sale.openStatus &&
            sale.startTime <= block.timestamp &&
            block.timestamp <= sale.endTime,
            "IcoManager.buy: Specified sale is not opened at the moment");

        bool isPublicSale = (keccak256(abi.encodePacked(saleId)) == keccak256(abi.encodePacked("PUBLIC_SALE")));
        require(isPublicSale || amount <= sale.buyerToAllowance[msg.sender],
            "IcoManager.buy: Buying amount exceeds allowance");

        uint256 payingAmount = amount * sale.price;
        require(msg.value >= payingAmount, "IcoManager.buy: Not enough value in transaction");

        IERC20(myToken).transfer(msg.sender, amount);
        payable(msg.sender).transfer(msg.value - payingAmount);

        if (!isPublicSale) {
            sale.buyerToAllowance[msg.sender] -= amount;
        }
        sale.totalSold += amount;

        emit Sold(saleId, msg.sender, amount);
    }

    /* ********************************************************************************************** */

    // Stop contract
    function stop() external onlyAdmin("stop") {
        stopped = true;
    }

    /* ********************************************************************************************** */

    // Owner withdraw ETH
    function withdraw(uint256 amount) external onlyAdmin("withdraw") {
        incomeReceiver.transfer(amount);
    }
}
