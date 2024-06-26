package com.image.fastcdc4j.internal.chunking;

import com.image.fastcdc4j.external.chunking.Chunker;

/**
 *
 * @Author litianwei
 * @Date 2024/1/22
 **/
public enum HashTables {
    ;

    @SuppressWarnings("OverlyLargePrimitiveArrayInitializer")
    private static final long[] NLFIEDLER_RUST =
        { 0x5c95c078L, 0x22408989L, 0x2d48a214L, 0x12842087L, 0x530f8afbL, 0x474536b9L, 0x2963b4f1L, 0x44cb738bL,
            0x4ea7403dL, 0x4d606b6eL, 0x074ec5d3L, 0x3af39d18L, 0x726003caL, 0x37a62a74L, 0x51a2f58eL,
            0x7506358eL, 0x5d4ab128L, 0x4d4ae17bL, 0x41e85924L, 0x470c36f7L, 0x4741cbe1L, 0x01bb7f30L,
            0x617c1de3L, 0x2b0c3a1fL, 0x50c48f73L, 0x21a82d37L, 0x6095ace0L, 0x419167a0L, 0x3caf49b0L,
            0x40cea62dL, 0x66bc1c66L, 0x545e1dadL, 0x2bfa77cdL, 0x6e85da24L, 0x5fb0bdc5L, 0x652cfc29L,
            0x3a0ae1abL, 0x2837e0f3L, 0x6387b70eL, 0x13176012L, 0x4362c2bbL, 0x66d8f4b1L, 0x37fce834L,
            0x2c9cd386L, 0x21144296L, 0x627268a8L, 0x650df537L, 0x2805d579L, 0x3b21ebbdL, 0x7357ed34L,
            0x3f58b583L, 0x7150ddcaL, 0x7362225eL, 0x620a6070L, 0x2c5ef529L, 0x7b522466L, 0x768b78c0L,
            0x4b54e51eL, 0x75fa07e5L, 0x06a35fc6L, 0x30b71024L, 0x1c8626e1L, 0x296ad578L, 0x28d7be2eL,
            0x1490a05aL, 0x7cee43bdL, 0x698b56e3L, 0x09dc0126L, 0x4ed6df6eL, 0x02c1bfc7L, 0x2a59ad53L,
            0x29c0e434L, 0x7d6c5278L, 0x507940a7L, 0x5ef6ba93L, 0x68b6af1eL, 0x46537276L, 0x611bc766L,
            0x155c587dL, 0x301ba847L, 0x2cc9dda7L, 0x0a438e2cL, 0x0a69d514L, 0x744c72d3L, 0x4f326b9bL,
            0x7ef34286L, 0x4a0ef8a7L, 0x6ae06ebeL, 0x669c5372L, 0x12402dcbL, 0x5feae99dL, 0x76c7f4a7L,
            0x6abdb79cL, 0x0dfaa038L, 0x20e2282cL, 0x730ed48bL, 0x069dac2fL, 0x168ecf3eL, 0x2610e61fL,
            0x2c512c8eL, 0x15fb8c06L, 0x5e62bc76L, 0x69555135L, 0x0adb864cL, 0x4268f914L, 0x349ab3aaL,
            0x20edfdb2L, 0x51727981L, 0x37b4b3d8L, 0x5dd17522L, 0x6b2cbfe4L, 0x5c47cf9fL, 0x30fa1ccdL,
            0x23dedb56L, 0x13d1f50aL, 0x64eddee7L, 0x0820b0f7L, 0x46e07308L, 0x1e2d1dfdL, 0x17b06c32L,
            0x250036d8L, 0x284dbf34L, 0x68292ee0L, 0x362ec87cL, 0x087cb1ebL, 0x76b46720L, 0x104130dbL,
            0x71966387L, 0x482dc43fL, 0x2388ef25L, 0x524144e1L, 0x44bd834eL, 0x448e7da3L, 0x3fa6eaf9L,
            0x3cda215cL, 0x3a500cf3L, 0x395cb432L, 0x5195129fL, 0x43945f87L, 0x51862ca4L, 0x56ea8ff1L,
            0x201034dcL, 0x4d328ff5L, 0x7d73a909L, 0x6234d379L, 0x64cfbf9cL, 0x36f6589aL, 0x0a2ce98aL,
            0x5fe4d971L, 0x03bc15c5L, 0x44021d33L, 0x16c1932bL, 0x37503614L, 0x1acaf69dL, 0x3f03b779L,
            0x49e61a03L, 0x1f52d7eaL, 0x1c6ddd5cL, 0x062218ceL, 0x07e7a11aL, 0x1905757aL, 0x7ce00a53L,
            0x49f44f29L, 0x4bcc70b5L, 0x39feea55L, 0x5242cee8L, 0x3ce56b85L, 0x00b81672L, 0x46beecccL,
            0x3ca0ad56L, 0x2396cee8L, 0x78547f40L, 0x6b08089bL, 0x66a56751L, 0x781e7e46L, 0x1e2cf856L,
            0x3bc13591L, 0x494a4202L, 0x520494d7L, 0x2d87459aL, 0x757555b6L, 0x42284cc1L, 0x1f478507L,
            0x75c95dffL, 0x35ff8dd7L, 0x4e4757edL, 0x2e11f88cL, 0x5e1b5048L, 0x420e6699L, 0x226b0695L,
            0x4d1679b4L, 0x5a22646fL, 0x161d1131L, 0x125c68d9L, 0x1313e32eL, 0x4aa85724L, 0x21dc7ec1L,
            0x4ffa29feL, 0x72968382L, 0x1ca8eef3L, 0x3f3b1c28L, 0x39c2fb6cL, 0x6d76493fL, 0x7a22a62eL,
            0x789b1c2aL, 0x16e0cb53L, 0x7deceeebL, 0x0dc7e1c6L, 0x5c75bf3dL, 0x52218333L, 0x106de4d6L,
            0x7dc64422L, 0x65590ff4L, 0x2c02ec30L, 0x64a9ac67L, 0x59cab2e9L, 0x4a21d2f3L, 0x0f616e57L,
            0x23b54ee8L, 0x02730aaaL, 0x2f3c634dL, 0x7117fc6cL, 0x01ac6f05L, 0x5a9ed20cL, 0x158c4e2aL,
            0x42b699f0L, 0x0c7c14b3L, 0x02bd9641L, 0x15ad56fcL, 0x1c722f60L, 0x7da1af91L, 0x23e0dbcbL,
            0x0e93e12bL, 0x64b2791dL, 0x440d2476L, 0x588ea8ddL, 0x4665a658L, 0x7446c418L, 0x1877a774L,
            0x5626407eL, 0x7f63bd46L, 0x32d2dbd8L, 0x3c790f4aL, 0x772b7239L, 0x6f8b2826L, 0x677ff609L,
            0x0dc82c11L, 0x23ffe354L, 0x2eac53a6L, 0x16139e09L, 0x0afd0dbcL, 0x2a4d4237L, 0x56a368c7L,
            0x234325e4L, 0x2dce9187L, 0x32e8ea7eL };

    @SuppressWarnings("OverlyLargePrimitiveArrayInitializer")
    private static final long[] RTPAL =
        { 0x5a16b18f2aac863eL, 0x05fad735784f09eaL, 0x355c6a3868fe64afL, 0x57df89c95716c702L, 0x46ea7572135544a6L,
            0x6291d5376cd79d73L, 0x2a6e072b609b0bbfL, 0x110f7f895ec438b7L, 0x2fc580f60659f690L,
            0x15ce33c924a8880bL, 0x1f3fabc44c091f5fL, 0x76e7512d0f53c142L, 0x30ff6d65448b44b3L,
            0x16db576e7ecfe3c9L, 0x7009bea841de2e20L, 0x0ad460d80f3fe181L, 0x0a1e6fed6ece42dbL,
            0x310d45cf0b0fdf5fL, 0x08a96d563dbf589eL, 0x0242bfc35fec28d5L, 0x627c8f391a430cf8L,
            0x6f6615ff6f6de700L, 0x6f8979754e271c9dL, 0x0859f64c2f67e948L, 0x63c2fd593e2f50cdL,
            0x36841c4d612cf902L, 0x10771d5b54e6391aL, 0x49b9ec670a771b9dL, 0x40bd85df748b9357L,
            0x2515b6965ec755c6L, 0x6e9fed0765bc944eL, 0x545b14b62bb0033eL, 0x1ffb4acd721cfb37L,
            0x2e11a5484f7e4d76L, 0x513cfa1d1409599fL, 0x667fc32675a9d9baL, 0x4b900ca83c6a130eL,
            0x6c883f7266b43d29L, 0x113f68df448dfb9bL, 0x610e6c284b1063c9L, 0x7ea51d3447a18deeL,
            0x1a0330843df0bbdbL, 0x539425db2defa9a3L, 0x6d516c376fad3325L, 0x3cc9a49e4e0b7534L,
            0x50a3d0312106b015L, 0x70fd64403ee483a5L, 0x7f789f326a75f2bdL, 0x7afca98213354a68L,
            0x023fedf465463724L, 0x79b02c7e2da5aa52L, 0x24c3c25b46d23042L, 0x0a6211f96f3f7007L,
            0x14d25f9364aed60bL, 0x232bbe945ecae09dL, 0x01bfcf37345dfd8dL, 0x01e47a9f5ee39461L,
            0x68faae243200a1ecL, 0x6c0a62fb6fca808cL, 0x79c5111c0a4f01d4L, 0x57e629f053268e22L,
            0x6637da42628a1d23L, 0x3a77374201bdd718L, 0x357af0823b8b08cfL, 0x6efd96b521cabfe4L,
            0x6406490661488eb8L, 0x2552e2ee3a5bc8d0L, 0x072b854c221af964L, 0x6dcef4e9443f337cL,
            0x0091f9a034af01cdL, 0x716fd3b3054e0bd0L, 0x1f5fbc1c6eb165c5L, 0x60e6856b540b51efL,
            0x1f11114e7c484faeL, 0x0000d09e058d4ad6L, 0x18e2758b749c1f10L, 0x75e885ce5eed60ccL,
            0x186390bc4089d41bL, 0x1a106a271448c1b7L, 0x0683a2b974697cf6L, 0x7c1fa0de32f4b108L,
            0x43c256022118b41aL, 0x0ad3169d2f4332eaL, 0x3de6dd9469e6b4cbL, 0x03214ec53d08b415L,
            0x47719437112e7492L, 0x54a5a37e24f2ecc0L, 0x1e672b6539cbdc95L, 0x63710b3b7c6a9138L,
            0x261906b85d188087L, 0x01a645217ab48b46L, 0x600aca562207be19L, 0x01c53fb56d5baabdL,
            0x6f77743452e4242bL, 0x62c146d02b9d58a8L, 0x4737fd8369b2ba58L, 0x440d74f45afe4f58L,
            0x309e12a057084cfbL, 0x19799d1500ddb160L, 0x0b4f9faf35698814L, 0x06ae60c0031acc89L,
            0x0f0bd5c06074337cL, 0x3280bd843dbfe2b4L, 0x79aa26532817b7d9L, 0x04c3231906d36fe7L,
            0x694a4eaa6f6d5f16L, 0x6971b68d494d1c6aL, 0x09f542094e8cc44cL, 0x02d7df3649d5ab34L,
            0x19b56823600284d6L, 0x333f0da127d2627dL, 0x6fad1e187fdd5340L, 0x0262e81653eade70L,
            0x4d544dad66a68a81L, 0x076d264a36c5dd17L, 0x78245a80673d88efL, 0x6e0a18da10b610e2L,
            0x3c32a91c5fdd6cc0L, 0x389edc3f5e3cc958L, 0x45b75df31f4a782dL, 0x5d1e968117a2ebdcL,
            0x2dc2ab5a0b68a79fL, 0x32e720973e523772L, 0x0bc636021a497728L, 0x1764a6b63fa13424L,
            0x22eb06cd5dc350feL, 0x2e8a233e69f6b146L, 0x5e58a3d53bd96d8bL, 0x694e34753307b070L,
            0x13ff2d4025effd64L, 0x47dab89f3a59b9aeL, 0x6bf66763145942afL, 0x0bdbbe1f443e0a55L,
            0x61fcf9652a245b4cL, 0x6a19a16d4be82702L, 0x3ad88e2e7482fef7L, 0x1b2aee5b64b1a96aL,
            0x476fa0f04de1ad0bL, 0x391770c140f198e7L, 0x2f521a79172b0d05L, 0x65864b7d3bed5d71L,
            0x785d0c7a77073370L, 0x48b96fc44b4849d1L, 0x741e87224ec165f3L, 0x4b67d9ca5b638351L,
            0x3e96913f2bbd6ed0L, 0x5afa027e3fb5a1e9L, 0x5b775fc205742763L, 0x4ed6cca60f781be3L,
            0x0de489eb0e32be40L, 0x0e50a4cd4dd4cd25L, 0x5193b326399d128cL, 0x105689474d7a367bL,
            0x6e99487e230e2428L, 0x4d54e9d05fd41632L, 0x587d30714e09df98L, 0x338578c64a0576a4L,
            0x5285f2b6253c8148L, 0x539bed9a237ea4b6L, 0x4cf4ef9c38c68c42L, 0x17c8be100cc5a8f3L,
            0x4cdc53e77f800cc1L, 0x604a1cc979463cacL, 0x2f2644603f5ad2f7L, 0x521bed84059390a7L,
            0x0a6a4d9131ae8eaeL, 0x37ee95f81465f059L, 0x6b5a472466c28910L, 0x64d391ba1ba64cfcL,
            0x2f9416a533ad2fe2L, 0x0ed532ff334c32dbL, 0x6c1ff320704db444L, 0x6cfddaa55a0c7e01L,
            0x7b9111f72d8db30dL, 0x1639e4ee0e2b053eL, 0x4b7ca73a4b5c22daL, 0x7b20510d45cc0b67L,
            0x04fd7dcb77857ba2L, 0x655190f26e03692aL, 0x722c89944077844cL, 0x7f990ad975187f50L,
            0x6dc7791921fafce9L, 0x2545283c54bfe0d2L, 0x138088684f05e2d1L, 0x373dcce0457d4a4dL,
            0x2f0a9f54154ad0ffL, 0x574f8d4818d0ffb8L, 0x50f1d3e804c93f0eL, 0x5fbd2b8c07db45f0L,
            0x691960cf770fdff8L, 0x31db755513e8d39cL, 0x1246e1003b30b0d5L, 0x5837c71a1501ea70L,
            0x67db6a3254bf1b57L, 0x3eb810bf66824135L, 0x78462ce501fd3358L, 0x079f7e61300bc19bL,
            0x0a63c6be07c9cbb7L, 0x012b3d262a85220cL, 0x365cb5ed3547ece6L, 0x24c194923ca64947L,
            0x60f1d96b430fa7e6L, 0x387badc5327750eaL, 0x01de5341400e9e27L, 0x03b3361536553067L,
            0x1a5e898a04dc5bd2L, 0x2a16207973e1c917L, 0x13b80dee498e46f6L, 0x282b6cb267f3ffc9L,
            0x1aa573c93edaa6ccL, 0x4724f7df33c54d9cL, 0x7b6470f71e0110c2L, 0x0eea5ab21cdf1d21L,
            0x169dce076f100ac5L, 0x3cb120ab66166d34L, 0x02ee08213534e408L, 0x204b064c0324391aL,
            0x1a55e5ef256f6050L, 0x228903333cc8b196L, 0x73c5fc5324fccf9bL, 0x1debf745427a832eL,
            0x2b38e7fe56472ce5L, 0x2c69b6592288b8ceL, 0x3e6a912353b96e40L, 0x0a6b2a4f6b1a413dL,
            0x09f0f0c2632b2016L, 0x5fa2dcc943d7bc78L, 0x76f44ebe0c29d7e4L, 0x7d85baca0aed3fb4L,
            0x562a53fd09ae5f20L, 0x0d836313270f989dL, 0x475a18725f305dc6L, 0x7893ff4e7f495128L,
            0x25fe3b134f4a865aL, 0x4c7f96e43712cba8L, 0x29caf4d32c543d7cL, 0x5b99aa853e46d124L,
            0x07a3e5aa538635e5L, 0x0e4b57d8155f9c88L, 0x2f71282b3c4fa210L, 0x798c81923d71487fL,
            0x45e90bc2494ad436L, 0x5291bcf62f0b6bdbL, 0x72ea193619f06853L, 0x5a5a2bd77114b311L,
            0x5445faa82e02e158L, 0x0065712926726beaL, 0x1bed3b9a62fbf757L, 0x1767b815257b83d4L,
            0x000eab4e77327b81L, 0x0fd333301966ff16L, 0x6780eb8339b83286L, 0x7652a5e647799673L,
            0x43c0db665e364315L, 0x6fe4fe01606d405dL, 0x6833dbd876b03920L };


    public static long[] getNlfiedlerRust() {
        return HashTables.NLFIEDLER_RUST.clone();
    }


    public static long[] getRtpal() {
        return HashTables.RTPAL.clone();
    }
}