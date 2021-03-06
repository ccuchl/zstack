package org.zstack.test.integration.kvm.vm

import org.zstack.compute.vm.VmSystemTags
import org.zstack.core.db.Q
import org.zstack.header.storage.snapshot.VolumeSnapshotTreeVO
import org.zstack.header.vm.VmInstanceVO
import org.zstack.sdk.ImageInventory
import org.zstack.sdk.InstanceOfferingInventory
import org.zstack.sdk.L3NetworkInventory
import org.zstack.sdk.VmInstanceInventory
import org.zstack.storage.snapshot.VolumeSnapshotGlobalConfig
import org.zstack.test.integration.kvm.Env
import org.zstack.test.integration.kvm.KvmTest
import org.zstack.testlib.EnvSpec
import org.zstack.testlib.SubCase

/**
 * Created by MaJin on 2017-10-19.
 */
class PerVmSnapshotMaxNumCase extends SubCase{
    EnvSpec env
    VmInstanceInventory vm1, vm2

    @Override
    void setup() {
        useSpring(KvmTest.springSpec)
    }

    @Override
    void environment() {
        env = Env.oneVmBasicEnv()
    }

    @Override
    void test() {
        env.create {
            vm1 = env.inventoryByName("vm") as VmInstanceInventory
            testSnapshotGlobalConfig()
            testVmSnapshotMaxNumSystemTag()
            testNoSystemTagCreateSnapshot()
            testPerVmSystemTagCreateSnapshot()
        }
    }

    @Override
    void clean() {
        env.delete()
    }

    void testSnapshotGlobalConfig(){
        updateGlobalConfig {
            category = VolumeSnapshotGlobalConfig.CATEGORY
            name = VolumeSnapshotGlobalConfig.MAX_INCREMENTAL_SNAPSHOT_NUM.name
            value = "2"
            sessionId = adminSession()
        }
        assert VolumeSnapshotGlobalConfig.MAX_INCREMENTAL_SNAPSHOT_NUM.value().equals("2")
    }

    void testVmSnapshotMaxNumSystemTag(){
        createSystemTag {
            resourceType = VmInstanceVO.getSimpleName()
            resourceUuid = vm1.uuid
            tag = "vmMaxIncrementalSnapshotNum::1"
        }
        assert VmSystemTags.VM_MAX_INCREMENTAL_SNAPSHOT_NUM.getTokenByResourceUuid(vm1.uuid,
                VmSystemTags.VM_MAX_INCREMENTAL_SNAPSHOT_NUM_TOKEN) == "1"
    }

    void testNoSystemTagCreateSnapshot(){
        def instanceOfferingInv = env.inventoryByName("instanceOffering") as InstanceOfferingInventory
        def imageInv = env.inventoryByName("image1") as ImageInventory
        def l3Inv = env.inventoryByName("l3") as L3NetworkInventory

        vm2 = createVmInstance {
            name = "vm2"
            instanceOfferingUuid = instanceOfferingInv.uuid
            imageUuid = imageInv.uuid
            l3NetworkUuids = [l3Inv.uuid]
        } as VmInstanceInventory

        // MaxNum for vm2 is not set, global config is 2
        assert Q.New(VolumeSnapshotTreeVO.class).count() == 0

        createVolumeSnapshot {
            volumeUuid = vm2.rootVolumeUuid
            name = "testVmSnapshot1"
        }

        createVolumeSnapshot {
            volumeUuid = vm2.rootVolumeUuid
            name = "testVmSnapshot2"
        }

        assert Q.New(VolumeSnapshotTreeVO.class).count() == 1

        createVolumeSnapshot {
            volumeUuid = vm2.rootVolumeUuid
            name = "testVmSnapshot2"
        }

        assert Q.New(VolumeSnapshotTreeVO.class).count() == 2
    }

    void testPerVmSystemTagCreateSnapshot(){
        // MaxNum for vm1 is 1, global config is 2
        int nowCount = Q.New(VolumeSnapshotTreeVO.class).count()

        createVolumeSnapshot {
            volumeUuid = vm1.rootVolumeUuid
            name = "testVmSnapshot1"
        }

        createVolumeSnapshot {
            volumeUuid = vm1.rootVolumeUuid
            name = "testVmSnapshot2"
        }

        assert Q.New(VolumeSnapshotTreeVO.class).count() == nowCount + 2
    }
}
