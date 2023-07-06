// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";

/**
 * @title MyToken
 * @dev 
 */
contract MyToken is ERC20 {
    constructor(address owner) ERC20("MyToken", "MTK") {
        _mint(owner, 1000000);
    }
}
