CREATE TABLE `UsbDeviceVO` (
  `uuid` varchar(32) NOT NULL UNIQUE COMMENT 'uuid',
  `name` varchar(2048) DEFAULT NULL,
  `description` varchar(2048) DEFAULT NULL,
  `hostUuid` varchar(32) NOT NULL,
  `vmInstanceUuid` varchar(32) DEFAULT NULL,
  `state` varchar(32) NOT NULL,
  `busNum` varchar(32) NOT NULL,
  `devNum` varchar(32) NOT NULL,
  `idVendor` varchar(32) NOT NULL,
  `idProduct` varchar(32) NOT NULL,
  `iManufacturer` varchar(1024) DEFAULT NULL,
  `iProduct` varchar(1024) DEFAULT NULL,
  `iSerial` varchar(32) DEFAULT NULL,
  `usbVersion` varchar(32) DEFAULT NULL,
  `lastOpDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP,
  `createDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`uuid`),
  CONSTRAINT fkUsbDeviceVOHostEO FOREIGN KEY (`hostUuid`) REFERENCES `zstack`.`HostEO` (`uuid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
